package com.example.locadine

// Info page with name, address, phone number, website and hours
// Users can add reviews to that restaurant
// should take intent from main activity and just display the place's info
/*
Restaurant info page with name + opening hours + reviews (from both Google API and our Firestore)
Users can add a review
 */

import android.content.Intent
import android.os.AsyncTask
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.squareup.picasso.Picasso
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.locadine.adapters.ReviewListAdapter
import com.example.locadine.pojos.Review
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.firestore.FirebaseFirestore
import io.grpc.android.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

data class Review(
    val userId: String, // ID of the user who wrote the review
    val userName: String, // Name of the user who wrote the review
    val rating: Float, // Rating (e.g., from 1 to 5)
    val comment: String, // Review comment
    val timestamp: Long // Timestamp when the review was created
)


class RestaurantPageActivity : AppCompatActivity() {

    private lateinit var textViewName: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewWebsite: TextView
    private lateinit var editTextUserReview: EditText
    private lateinit var buttonSubmitReview: Button

    private lateinit var hoursSpinner: Spinner
    private lateinit var selectedHoursTextView: TextView

    // Reviews
    private lateinit var arrayList: ArrayList<Review>
    private lateinit var arrayAdapter: ReviewListAdapter
    private lateinit var reviewListView: ListView
    private lateinit var db: FirebaseFirestore


    private lateinit var placeID: String

    //private val mapsAPIKey2 = MAPS_API_KEY
    private val mapsAPIKey = BuildConfig.MAPS_API_KEY       // fix

    private val coroutineScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(applicationContext, mapsAPIKey)
        setContentView(R.layout.activity_restaurant)

        db = FirebaseFirestore.getInstance()

        textViewName = findViewById(R.id.textViewRestaurantName)
        textViewAddress = findViewById(R.id.address_id)
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber)
        textViewWebsite = findViewById(R.id.textViewWebsite)
        hoursSpinner = findViewById(R.id.hoursSpinner)
        //selectedHoursTextView = findViewById(R.id.selectedHoursTextView)

        buttonSubmitReview = findViewById(R.id.buttonSubmitReview)

        textViewName.text = "Restaurant Name"
        textViewAddress.text = "123 Main St, City, Country"
        textViewPhoneNumber.text = "Phone: 123-456-7890"
        textViewWebsite.text = "Website: www.restaurant.com"

        reviewListView = findViewById(R.id.review_list_restaurant)

        arrayList = ArrayList()
        arrayAdapter = ReviewListAdapter(this, arrayList)
        reviewListView.adapter = arrayAdapter

        db = FirebaseFirestore.getInstance()

        fetchReviews()

        val imageViewBig: ImageView = findViewById(R.id.imageViewBig)
        val imageViewSmall1: ImageView = findViewById(R.id.imageViewSmall1)
        val imageViewSmall2: ImageView = findViewById(R.id.imageViewSmall2)

        imageViewBig.setImageResource(R.drawable.ic_stockimage)
        imageViewSmall1.setImageResource(R.drawable.ic_pasta)
        imageViewSmall2.setImageResource(R.drawable.ic_storeinterior)

        //var placeID = "ChIJ0Vd-nPtYwokRTyT-MFTBXdg"
        var placeID = "ChIJA5fDJDh0hlQRJ-sp7u9Y8p8"

        val placesClient = Places.createClient(this)
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.WEBSITE_URI,
            Place.Field.OPENING_HOURS
        )
        val request = FetchPlaceRequest.newInstance(placeID, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                println("debug: Place Details: $place")

                // Update TextViews with place information
                textViewName.text = "Restaurant Name: ${place.name}"
                textViewAddress.text = "Address: ${place.address}"
                textViewPhoneNumber.text = "Phone: ${place.phoneNumber}"
                textViewWebsite.text = "Website: ${place.websiteUri}"

            }
            .addOnFailureListener { exception ->
                // Handle the failure
                println("Error fetching place details: $exception")
            }


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
}



