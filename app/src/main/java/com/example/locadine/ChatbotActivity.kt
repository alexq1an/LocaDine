package com.example.locadine

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locadine.ViewModels.RestaurantInfoViewModel
import com.example.locadine.adapters.MessageAdapter
import com.example.locadine.api.OpenAIApiService
import com.example.locadine.pojos.Message
import com.example.locadine.pojos.OpenAIMessage
import com.example.locadine.pojos.OpenAIRequest
import com.example.locadine.pojos.OpenAIResponse
import com.example.locadine.pojos.OpenAIRoleType
import com.example.locadine.pojos.RestaurantInfo
import com.example.locadine.pojos.SenderType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatbotActivity : AppCompatActivity() {
    private lateinit var chatView: RecyclerView
    private lateinit var sendMessageButton: ImageButton
    private lateinit var userMessageEditText: EditText

    private lateinit var messageArrayList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var openAIApiService: OpenAIApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        chatView = findViewById(R.id.chatbot)
        sendMessageButton = findViewById(R.id.chat_send_button)
        userMessageEditText = findViewById(R.id.chat_message)

        messageArrayList = ArrayList()

        sendMessageButton.setOnClickListener(View.OnClickListener {
            if (userMessageEditText.text.isBlank()) {
                Toast.makeText(this, "Message cannot be blank", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(userMessageEditText.text.toString())
                userMessageEditText.setText("")
            }
        })

        messageAdapter = MessageAdapter(messageArrayList, this)
        chatView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        chatView.adapter = messageAdapter

        val retrofit = Util.getOpenAIRetrofitInstance()
        openAIApiService = retrofit.create(OpenAIApiService::class.java)

        addMessageAndScroll(Message("Hi there, I am a chatbot powered by OpenAI's GTP 3.5 model. " +
                "I have access to details of restaurants near you such as name, price level" +
                ", average rating, and top reviews. Ask me any questions and I'll try my best to answer :)", SenderType.BOT))
    }

    private fun sendMessage(userMessage: String) {
        addMessageAndScroll(Message(userMessage, SenderType.USER))

        val request = OpenAIRequest(
            model="gpt-3.5-turbo",
            messages = listOf(OpenAIMessage(OpenAIRoleType.USER.type, getPrePrompt() + userMessage)),
            temperature = 0.7
        )

        val call = openAIApiService.sendMessageToOpenAI(request)
        addMessageAndScroll(Message("Thinking...", SenderType.BOT))

        call.enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(call: Call<OpenAIResponse>, response: Response<OpenAIResponse>) {
                removeLastMessage()
                if (response.isSuccessful) {
                    val botResponse = response.body()!!.choices[0].message.content
                    addMessageAndScroll(Message(botResponse, SenderType.BOT))
                } else {
                    Toast.makeText(this@ChatbotActivity, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                removeLastMessage()
                Toast.makeText(this@ChatbotActivity, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMessageAndScroll(message: Message) {
        messageArrayList.add(message)
        messageAdapter.notifyDataSetChanged()
        chatView.scrollToPosition(messageAdapter.itemCount - 1)
    }

    private fun removeLastMessage() {
        messageArrayList.removeLast()
        messageAdapter.notifyDataSetChanged()
    }

    private fun getPrePrompt(): String {
        return getRestaurantInfo() + getChatHistory() + "Create an answer that uses an average of 50 tokens but if needed you can use a maximum of 100 tokens\n\n"
    }

    private fun getChatHistory(): String {
        var history = "Here is our chat history so far:\n\n"
        for (message in messageArrayList) {
            if (message.sender == SenderType.USER) {
                history += "Me: ${message.message}\n\n"
            } else {
                history += "You: ${message.message}\n\n"
            }
        }
        return history
    }

    private fun getRestaurantInfo(): String {
        val restaurants = RestaurantInfoViewModel.restaurantInfoList
        return if (restaurants.isNotEmpty()) {
            "Here is information about nearby restaurants:\n\n" + getRestaurantsSummary(restaurants)
        } else {
            ""
        }
    }

    private fun getRestaurantsSummary(restaurants: List<RestaurantInfo>): String {
        var result = ""

        restaurants.forEach {
            result += "Name: ${it.name}\n"
            result += "Open now? ${it.opening_hours?.open_now}\n"
            result += "Price level: ${priceLevelToText(it.price_level)}\n"
            result += "Business status: ${it.business_status}\n"
            result += "Average rating: ${it.rating}\n"
            result += "Number of ratings: ${it.user_ratings_total}\n"
            result += "Reviews:\n" + getReviews(it)
        }

        return result
    }

    private fun getReviews(restaurant: RestaurantInfo): String {
        var result = ""

        for (i in 0..5) {
            if (restaurant.reviews == null || restaurant.reviews!!.size <= i) {
                break
            }
            val review = restaurant.reviews!![i]
            result += "${review.author_name} gave this restaurant a ${review.rating} stars review with the following comment: ${review.text}\n\n"
        }

        return result
    }

    private fun priceLevelToText(level: Int?) : String {
        if (level == null) {
            return "Unknown"
        } else if (level == 1) {
            return "Cheap"
        } else if (level == 2) {
            return "Moderate"
        } else if (level == 3) {
            return "Expensive"
        } else {
            return "Very expensive"
        }
    }
}