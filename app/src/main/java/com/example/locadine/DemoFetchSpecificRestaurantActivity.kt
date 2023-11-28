package com.example.locadine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.locadine.api.GooglePlacesAPIService
import com.example.locadine.pojos.GetPlaceDetailsResponse
import com.example.locadine.pojos.RestaurantInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DemoFetchSpecificRestaurantActivity : AppCompatActivity() {
    private lateinit var googlePlacesAPIService: GooglePlacesAPIService

    private lateinit var restaurantsSummary: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_fetch_specific_restaurant)

        restaurantsSummary = findViewById(R.id.specific_restaurants_details)
        imageView = findViewById(R.id.specific_restaurant_image)

        val retrofit = Util.getGooglePlacesRetrofitInstance()
        googlePlacesAPIService = retrofit.create(GooglePlacesAPIService::class.java)

        sendRequest()
    }

    private fun sendRequest() {
        val placeId = "ChIJBRXCcsB5hlQRewtNTQuhEoI"
        val call = googlePlacesAPIService.getPlaceDetails(placeId, BuildConfig.MAPS_API_KEY)
        call.enqueue(object : Callback<GetPlaceDetailsResponse> {
            override fun onResponse(call: Call<GetPlaceDetailsResponse>, response: Response<GetPlaceDetailsResponse>) {
                if (response.isSuccessful) {
                    val restaurant = response.body()!!.result
                    restaurantsSummary.text = getRestaurantSummary(restaurant)

                    val photoUrl = Util.getPhotoUrl(restaurant.photos!![0].photo_reference)
                    Glide.with(this@DemoFetchSpecificRestaurantActivity)
                        .load(photoUrl)
                        .into(imageView)
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<GetPlaceDetailsResponse>, t: Throwable) {
                // Handle the failure case, such as a network error
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