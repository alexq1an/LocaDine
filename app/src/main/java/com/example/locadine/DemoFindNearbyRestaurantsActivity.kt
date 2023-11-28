package com.example.locadine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.locadine.api.GooglePlacesAPIService
import com.example.locadine.pojos.NearbySearchResponse
import com.example.locadine.pojos.RestaurantInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DemoFindNearbyRestaurantsActivity : AppCompatActivity() {
    private lateinit var googlePlacesAPIService: GooglePlacesAPIService

    private lateinit var restaurantsSummary: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_find_nearby_restaurants)

        restaurantsSummary = findViewById(R.id.specific_restaurants_details)
        imageView = findViewById(R.id.specific_restaurant_image)

        val retrofit = Util.getGooglePlacesRetrofitInstance()
        googlePlacesAPIService = retrofit.create(GooglePlacesAPIService::class.java)

        sendRequest()
    }

    private fun sendRequest() {
        val location = "49.2768,-122.9180" // SFU location
        val radiusInMeters = 1000
        val call = googlePlacesAPIService.findNearbyRestaurants(location, radiusInMeters, BuildConfig.MAPS_API_KEY)
        call.enqueue(object : Callback<NearbySearchResponse> {
            override fun onResponse(call: Call<NearbySearchResponse>, response: Response<NearbySearchResponse>) {
                if (response.isSuccessful) {
                    val restaurants = response.body()!!.results
                    restaurantsSummary.text = getRestaurantsSummary(restaurants)

                    val photoUrl = Util.getPhotoUrl(restaurants[0].photos!![0].photo_reference)
                    Glide.with(this@DemoFindNearbyRestaurantsActivity)
                        .load(photoUrl)
                        .into(imageView)
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<NearbySearchResponse>, t: Throwable) {
                // Handle the failure case, such as a network error
            }
        })
    }

    private fun getRestaurantsSummary(restaurants: List<RestaurantInfo>): String {
        var result = "\nRestaurants near SFU:\n\n"

        restaurants.forEach {
            result += "Name: ${it.name}\n"
            result += "Id: ${it.place_id}\n"
            result += "Open now? ${it.opening_hours?.open_now}\n"
            result += "Price level: ${it.price_level}\n"
            result += "Business status: ${it.business_status}\n"
            result += "Average rating: ${it.rating}\n"
            result += "Number of ratings: ${it.user_ratings_total}\n"
            result += "Latitude: ${it.geometry.location.lat}, longitude: ${it.geometry.location.lng}\n\n"
        }

        return result
    }
}