package com.example.locadine.api

import com.example.locadine.pojos.GetPlaceDetailsResponse
import com.example.locadine.pojos.NearbySearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesAPIService {
    @GET("maps/api/place/nearbysearch/json")
    fun findNearbyRestaurants(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") apiKey: String,
        @Query("type") type: String = "restaurant"
    ): Call<NearbySearchResponse>

    @GET("maps/api/place/details/json")
    fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): Call<GetPlaceDetailsResponse>
}
