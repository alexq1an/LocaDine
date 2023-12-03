package com.example.locadine.pojos

import java.util.Date

data class NearbySearchResponse(
    val results: List<RestaurantInfo>,
    val status: String
)

data class GetPlaceDetailsResponse(
    val result: RestaurantInfo,
    val status: String
)

data class RestaurantInfo(
    val business_status: String,
    val geometry: Geometry,
    val name: String,
    val opening_hours: OpeningHours?,
    val photos: List<Photo>?,
    val place_id: String,
    val rating: Double?,
    val types: List<String>,
    val user_ratings_total: Int?,
    val vicinity: String,
    val price_level: Int?,
    val reviews: List<GoogleReview>?,
    val website : String,
    val formatted_phone_number : String,
)

data class Geometry(
    val location: Location,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

data class OpeningHours(
    val open_now: Boolean,
    val periods: List<Period>,
    val weekday_text: List<String>
)

data class Period(
    val close: Close,
    val open: Open
)

data class Close(
    val day: Int,
    val time: String
)

data class Open(
    val day: Int,
    val time: String
)

data class Photo(
    val height: Int,
    val html_attributions: List<String>,
    val photo_reference: String,
    val width: Int
)

data class GoogleReview(
    val restaurantName: String,
    val review : String,
    val author_name: String,
    val author_url: String,
    val reviewerEmail:  String,
    val createdAt : String,
    val language: String,
    val original_language: String,
    val profile_photo_url: String,
    val rating: Int,
    val relative_time_description: String,
    val text: String,
    val time: Long,
    val translated: Boolean,
    val date: Date,
)