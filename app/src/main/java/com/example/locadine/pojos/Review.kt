package com.example.locadine.pojos

import com.google.firebase.Timestamp
import java.util.Date

data class Review(
    val reviewerEmail: String,
    val restaurantName: String,
    val review: String,
    val rating: Float,
    val createdAt: Date,
    val placeID : String,
) {
    constructor(data: Map<String, Any>) : this(
        reviewerEmail = data["reviewerEmail"] as String,
        restaurantName = data["restaurantName"] as String,
        review = data["review"] as String,
        rating = (data["rating"] as Double).toFloat(),
        createdAt = (data["createdAt"] as Timestamp).toDate(),
        placeID = data["placeID"] as? String ?: ""
    )
}
