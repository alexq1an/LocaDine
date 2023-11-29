import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.core.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.locadine.adapters.ReviewListAdapter
import com.example.locadine.pojos.Review
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import io.grpc.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.example.locadine.AddReviewActivity
import com.example.locadine.R
import com.example.locadine.Util
import com.example.locadine.api.GooglePlacesAPIService
import com.example.locadine.pojos.GetPlaceDetailsResponse
import com.example.locadine.pojos.RestaurantInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.lang.Exception
class RestaurantPageActivity : AppCompatActivity() {

    private lateinit var textViewName: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewWebsite: TextView
    private lateinit var editTextUserReview: EditText
    private lateinit var buttonSubmitReview: Button
    private lateinit var restaurantsSummary: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView


    private lateinit var hoursSpinner: Spinner

    // Reviews
    private lateinit var arrayList: ArrayList<Review>
    private lateinit var arrayAdapter: ReviewListAdapter
    private lateinit var reviewListView: ListView
    private lateinit var db: FirebaseFirestore
    private lateinit var googlePlacesAPIService: GooglePlacesAPIService

    private val coroutineScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_restaurant)

        db = FirebaseFirestore.getInstance()

        textViewName = findViewById(R.id.textViewRestaurantName)
        textViewAddress = findViewById(R.id.address_id)
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber)
        textViewWebsite = findViewById(R.id.textViewWebsite)
        hoursSpinner = findViewById(R.id.hoursSpinner)
        restaurantsSummary = findViewById(R.id.textViewSummary)

        //selectedHoursTextView = findViewById(R.id.selectedHoursTextView)

        buttonSubmitReview = findViewById(R.id.buttonSubmitReview)

        imageView1 = findViewById(R.id.imageViewBig)
        imageView2 = findViewById(R.id.imageViewSmall1)
        imageView3 = findViewById(R.id.imageViewSmall2)

        reviewListView = findViewById(R.id.review_list_restaurant)

        arrayList = ArrayList()
        arrayAdapter = ReviewListAdapter(this, arrayList)
        reviewListView.adapter = arrayAdapter

        val retrofit = Util.getGooglePlacesRetrofitInstance()
        googlePlacesAPIService = retrofit.create(GooglePlacesAPIService::class.java)

        // Extract PlaceID from Intent
        val placeId = intent.getStringExtra("PLACE_ID")
        placeId?.let {
            sendRequest(it)
        }

        db = FirebaseFirestore.getInstance()

        fetchReviews()


        lifecycleScope.launch(Dispatchers.Main) {
            ArrayAdapter.createFromResource(
                applicationContext, R.array.days_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                hoursSpinner.adapter = adapter
            }


            hoursSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {   }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            buttonSubmitReview.setOnClickListener {
                openAddReviewActivity()

            }

        }
    }

    private fun sendRequest(placeId: String) {
        val call = googlePlacesAPIService.getPlaceDetails(placeId, com.example.locadine.BuildConfig.MAPS_API_KEY)
        call.enqueue(object : Callback<GetPlaceDetailsResponse> {
            override fun onResponse(call: Call<GetPlaceDetailsResponse>, response: Response<GetPlaceDetailsResponse>) {
                if (response.isSuccessful) {
                    val restaurant = response.body()!!.result
                    restaurantsSummary.text = getRestaurantSummary(restaurant)

                    val photoUrl = Util.getPhotoUrl(restaurant.photos!![0].photo_reference)
                    Glide.with(this@RestaurantPageActivity)
                        .load(photoUrl)
                        .into(imageView1)
                } else {
                    // Handle error
                    Toast.makeText(this@RestaurantPageActivity, "Error fetching restaurant details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetPlaceDetailsResponse>, t: Throwable) {
                // Handle the failure case, such as a network error
                Toast.makeText(this@RestaurantPageActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun openAddReviewActivity() {
        val intent = Intent(this, AddReviewActivity::class.java)

        startActivityForResult(intent, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }


    private fun fetchReviews() {
        db.collection("reviews").get().addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful) {
                val reviews = it.result.documents.map { doc -> Review(doc.data as Map<String, Any>) }
                val sortedReviews = reviews.sortedByDescending { it -> it.createdAt }
                arrayAdapter.replace(sortedReviews)
                arrayAdapter.notifyDataSetChanged()
            } else {
                val errorMessage = it.exception?.message
                Toast.makeText(this, "Failed to fetch reviews. $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getRestaurantSummary(restaurant: RestaurantInfo): String {
        var result = ""

        result += "Name: ${restaurant.name}\n"
        result += "Id: ${restaurant.place_id}\n"
        result += "Open now? ${restaurant.opening_hours?.open_now}\n"
        result += "Price level: ${restaurant.price_level}\n"
        result += "Business status: ${restaurant.business_status}\n"
        result += "Average rating: ${restaurant.rating}\n"
        result += "Number of ratings: ${restaurant.user_ratings_total}\n"
        result += "Latitude: ${restaurant.geometry.location.lat}, longitude: ${restaurant.geometry.location.lng}\n\n"
        result += "One review: Author='${restaurant.reviews?.get(1)?.author_name}' Content='${restaurant.reviews?.get(0)?.text}'\n\n"

        return result
    }

}



