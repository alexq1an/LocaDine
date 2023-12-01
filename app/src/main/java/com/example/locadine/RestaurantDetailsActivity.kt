package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.locadine.adapters.ReviewListAdapter
import com.example.locadine.api.GooglePlacesAPIService
import com.example.locadine.pojos.GetPlaceDetailsResponse
import com.example.locadine.pojos.RestaurantInfo
import com.example.locadine.pojos.Review
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
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
// make review relevant to only restaurant
// add favourite button
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
    private lateinit var restaurantID: String
    private lateinit var restaurant: RestaurantInfo
    // Reviews
    private lateinit var arrayList: ArrayList<Review>
    private lateinit var arrayAdapter: ReviewListAdapter
    private lateinit var reviewListView: ListView
    private lateinit var db: FirebaseFirestore
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var googlePlacesAPIService: GooglePlacesAPIService
    private var favouriteFlag = false

    private val coroutineScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_restaurant_details)

        db = FirebaseFirestore.getInstance()
        fbAuth = FirebaseAuth.getInstance()

        textViewName = findViewById(R.id.textViewRestaurantName)
        textViewAddress = findViewById(R.id.address_id)
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber)
        textViewWebsite = findViewById(R.id.textViewWebsite)
        hoursSpinner = findViewById(R.id.hoursSpinner)
        restaurantsSummary = findViewById(R.id.textViewSummary)
        favouriteButton = findViewById(R.id.favourite_button)

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
        restaurantID = intent.getStringExtra("PLACE_ID")!!
        sendRequest(restaurantID)

        // this is declared twice?
        db = FirebaseFirestore.getInstance()

        fetchReviews()
        checkIfFavourite()


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

        favouriteButton.setOnClickListener {
            if (fbAuth != null ){
                if (!favouriteFlag) {
                    addFavourite()
                    Toast.makeText(this, "Saved to favourites!", Toast.LENGTH_SHORT).show()
                }
                else if(favouriteFlag) {
                    removeFavourite()
                    Toast.makeText(this, "Removed from favourites!", Toast.LENGTH_SHORT).show()
                }

            }
            else {
                Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT).show()
            }

        }
    }



    private fun sendRequest(placeId: String) {
        val call = googlePlacesAPIService.getPlaceDetails(placeId, com.example.locadine.BuildConfig.MAPS_API_KEY)
        call.enqueue(object : Callback<GetPlaceDetailsResponse> {
            override fun onResponse(call: Call<GetPlaceDetailsResponse>, response: Response<GetPlaceDetailsResponse>) {
                if (response.isSuccessful) {
                    restaurant = response.body()!!.result
                    restaurantsSummary.text = getRestaurantSummary(restaurant)  // separate summary

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


    private fun fetchReviews() {
        db.collection("reviews").get().addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful) {
                val reviews = it.result.documents.map { doc -> Review(doc.data as Map<String, Any>) }
                val sortedReviews = reviews.sortedByDescending { review -> review.createdAt }
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
        //result += "Latitude: ${restaurant.geometry.location.lat}, longitude: ${restaurant.geometry.location.lng}\n\n"
        //result += "One review: Author='${restaurant.reviews?.get(1)?.author_name}' Content='${restaurant.reviews?.get(0)?.text}'\n\n"

        return result
    }

    private fun addFavourite() {
        db.collection("users").document(fbAuth.uid!!).collection("favourites").document().set(
            hashMapOf(
                "restaurantID" to restaurantID,
                "restaurantName" to restaurant.name,
                "restaurantPicture" to Util.getPhotoUrl(restaurant.photos!![0].photo_reference),
            )
        )
        favouriteButton.text = "Unfavourite"
        favouriteFlag = true
    }

    private fun removeFavourite() {
        val favCollection = db.collection("users").document(fbAuth.uid!!).collection("favourites")
        val query = favCollection.whereEqualTo("restaurantID", restaurantID)
        query.get().addOnCompleteListener(this) { res ->
            if (res.isSuccessful) {
                val ref = db.collection("users").document(fbAuth.uid!!).collection("favourites")
                    .document(res.result.documents[0].id)
                ref.delete().addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Removed from favourites!", Toast.LENGTH_SHORT).show()
                        favouriteButton.text = "Favourite"
                        favouriteFlag = false
                    } else {
                        println("Document not found")
                    }
                }
            } else {
                println("Document not found")
            }
        }
    }

    private fun checkIfFavourite() {
        //checks if user already has this restaurant in favourites
        val favCollection = db.collection("users").document(fbAuth.uid!!).collection("favourites")
        val query = favCollection.whereEqualTo("restaurantID", restaurantID)
        query.get().addOnCompleteListener(this) {
            
            // set unfavourite only if the result is successful and the restaurant exist
            if (it.isSuccessful && !it.result.documents.isEmpty()) {

                favouriteButton.text = "Unfavourite"
                favouriteFlag = true
            } else {
                favouriteButton.text = "Favourite"
                favouriteFlag = false
            }
        }

    }

}