package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.locadine.adapters.GoogleReviewListAdapter
import com.example.locadine.adapters.ReviewListAdapter
import com.example.locadine.api.GooglePlacesAPIService
import com.example.locadine.pojos.GetPlaceDetailsResponse
import com.example.locadine.pojos.GoogleReview
import com.example.locadine.pojos.RestaurantInfo
import com.example.locadine.pojos.Review
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// need rating bar that shows stars for price and rating
// organize summary to be more readable
// make reviews scrollable
// make hours accurate to real store hours


class RestaurantDetailsActivity : AppCompatActivity() {
    private lateinit var textViewName: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewWebsite: TextView
    private lateinit var buttonSubmitReview: Button
    private lateinit var favouriteButton : Button
    private lateinit var restaurantsSummary: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var hoursSpinner: Spinner
    private lateinit var ratingBar: RatingBar
    private val restaurant_name = "Restaurant Name"

    // Reviews
    private lateinit var arrayList: ArrayList<Review>
    private lateinit var arrayAdapter: ReviewListAdapter
    private lateinit var reviewListView: ListView

    private lateinit var arrayListGoogle: ArrayList<GoogleReview>
    private lateinit var arrayAdapterGoogle : GoogleReviewListAdapter


    private lateinit var db: FirebaseFirestore
    private lateinit var googlePlacesAPIService: GooglePlacesAPIService

    private val coroutineScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_restaurant_details)

        db = FirebaseFirestore.getInstance()

        textViewName = findViewById(R.id.textViewRestaurantName)
        textViewAddress = findViewById(R.id.address_id)
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber)
        textViewWebsite = findViewById(R.id.textViewWebsite)
        hoursSpinner = findViewById(R.id.hoursSpinner)
        restaurantsSummary = findViewById(R.id.textViewSummary)
        favouriteButton = findViewById(R.id.favourite_button)
        ratingBar = findViewById(R.id.ratingBar)
        ratingBar.stepSize = 0.5f
        ratingBar.rating = 4f

        //selectedHoursTextView = findViewById(R.id.selectedHoursTextView)

        buttonSubmitReview = findViewById(R.id.buttonSubmitReview)

        imageView1 = findViewById(R.id.imageViewBig)
        imageView2 = findViewById(R.id.imageViewSmall1)
        imageView3 = findViewById(R.id.imageViewSmall2)

        reviewListView = findViewById(R.id.review_list_firebase)

        arrayList = ArrayList()
        arrayAdapter = ReviewListAdapter(this, arrayList)
        reviewListView.adapter = arrayAdapter


        arrayListGoogle = ArrayList()
        arrayAdapterGoogle = GoogleReviewListAdapter(this, arrayListGoogle)
        val recyclerView: RecyclerView = findViewById(R.id.google_review_list)
        recyclerView.adapter = arrayAdapterGoogle
        recyclerView.layoutManager = LinearLayoutManager(this)

        /*
        reviewListViewGoogle.adapter = arrayAdapterGoogle
         */

        val retrofit = Util.getGooglePlacesRetrofitInstance()
        googlePlacesAPIService = retrofit.create(GooglePlacesAPIService::class.java)

        // Extract PlaceID from Intent
        val placeId = intent.getStringExtra("PLACE_ID")
        placeId?.let {
            sendRequest(it)
        }

        db = FirebaseFirestore.getInstance()

        //fetchReviews()


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
                    restaurantsSummary.text = getRestaurantSummary(restaurant)  // separate summary
                    textViewName.text = restaurant.name
                    textViewAddress.text = restaurant.vicinity
                    ratingBar.rating = restaurant.rating?.toFloat() ?: 3f
                    restaurant_name == restaurant.name
                    textViewPhoneNumber.text = restaurant.formatted_phone_number
                    textViewWebsite.text = "Website : ${restaurant.website}"


                    val photoUrl1 = Util.getPhotoUrl(restaurant.photos!![0].photo_reference)
                    Glide.with(this@RestaurantDetailsActivity)
                        .load(photoUrl1)
                        .into(imageView1)

                    val photoUrl2 = Util.getPhotoUrl(restaurant.photos!![1].photo_reference)
                    Glide.with(this@RestaurantDetailsActivity)
                        .load(photoUrl2)
                        .into(imageView2)

                    // Load image into imageView3
                    val photoUrl3 = Util.getPhotoUrl(restaurant.photos!![2].photo_reference)
                    Glide.with(this@RestaurantDetailsActivity)
                        .load(photoUrl3)
                        .into(imageView3)

                    val googleReviews = response.body()?.result?.reviews
                    googleReviews?.let {
                        arrayListGoogle.clear()
                        arrayListGoogle.addAll(it)
                        arrayAdapterGoogle.notifyDataSetChanged()
                    }

                    fetchReviews(restaurant.name, placeId)
                } else {
                    // Handle error
                    Toast.makeText(this@RestaurantDetailsActivity, "Error fetching restaurant details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetPlaceDetailsResponse>, t: Throwable) {
                // Handle the failure case, such as a network error
                Toast.makeText(this@RestaurantDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
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



    private fun fetchReviews(restaurantName: String, placeID: String) {
        Toast.makeText(this, "${textViewName.text}", Toast.LENGTH_SHORT).show()
        db.collection("reviews").whereEqualTo("restaurantName", restaurantName).get()
            .addOnCompleteListener(OnCompleteListener {
                if (it.isSuccessful) {
                    val reviews =
                        it.result.documents.map { doc -> Review(doc.data as Map<String, Any>) }
                    val sortedReviews = reviews.sortedByDescending { review -> review.createdAt }
                    arrayAdapter.replace(sortedReviews)
                    arrayAdapter.notifyDataSetChanged()
                } else {
                    val errorMessage = it.exception?.message
                    Toast.makeText(
                        this,
                        "Failed to fetch reviews. $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val call = googlePlacesAPIService.getPlaceDetails(placeID,
            com.example.locadine.BuildConfig.MAPS_API_KEY
        )
        call.enqueue(object : Callback<GetPlaceDetailsResponse> {
            override fun onResponse(
                call: Call<GetPlaceDetailsResponse>,
                response: Response<GetPlaceDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    val googleReviews = response.body()?.result?.reviews
                    googleReviews?.let {
                        arrayListGoogle.clear()
                        arrayListGoogle.addAll(it)
                        arrayAdapterGoogle.notifyDataSetChanged()
                    }

                    // Debugging: Print the first GoogleReview to check the data
                    Log.d("GoogleReview", "First GoogleReview: ${googleReviews?.firstOrNull()}")
                } else {
                    // Handle error
                    Toast.makeText(
                        this@RestaurantDetailsActivity,
                        "Error fetching restaurant details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetPlaceDetailsResponse>, t: Throwable) {
                // Handle the failure case, such as a network error
                Toast.makeText(this@RestaurantDetailsActivity, "Network error", Toast.LENGTH_SHORT)
                    .show()
            }
        })

                fun onFailure(call: Call<GetPlaceDetailsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@RestaurantDetailsActivity,
                        "Network error while fetching Google reviews",
                        Toast.LENGTH_SHORT
                    ).show()
                }
    }



        private fun getRestaurantSummary(restaurant: RestaurantInfo): String {
        var result = ""

        //result += "Summary : ${restaurant.types}\n}"
        result += "Price level: ${restaurant.price_level}\n"
        //result += "Phone number: ${restaurant.formatted_phone_number}\n"
        //result += "Website : ${restaurant.website}\n"
        //result += "Name: ${restaurant.name}\n"
        //result += "Id: ${restaurant.place_id}\n"
        //result += "Open now? ${restaurant.opening_hours?.open_now}\n"
        //result += "Price level: ${restaurant.price_level}\n"
        //result += "Business status: ${restaurant.business_status}\n"
        //result += "Average rating: ${restaurant.rating}\n"
        //result += "Number of ratings: ${restaurant.user_ratings_total}\n"
        //result += "Address: ${restaurant.vicinity}\n"

        return result
    }
}





