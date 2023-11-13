package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import com.example.locadine.pojos.Review
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

class AddReviewActivity : AppCompatActivity() {
    private lateinit var restaurantName: EditText
    private lateinit var review: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var submitReviewButton: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        restaurantName = findViewById(R.id.restaurant_name)
        review = findViewById(R.id.restaurant_review)

        ratingBar = findViewById(R.id.ratingBar)
        ratingBar.stepSize = 0.5f
        ratingBar.rating = 4f

        submitReviewButton = findViewById(R.id.submit_review_button)
        submitReviewButton.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val restaurantNameText = restaurantName.text.toString()
        val reviewText = restaurantName.text.toString()
        val ratingValue = ratingBar.rating

        if (restaurantNameText.isBlank()) {
            Toast.makeText(this, "Restaurant name cannot be empty", Toast.LENGTH_SHORT).show()
        } else if (reviewText.isBlank()) {
            Toast.makeText(this, "Review cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            val userEmail = auth.currentUser!!.email!!
            val reviewData = Review(userEmail, restaurantNameText, reviewText, ratingValue, Date())
            db.collection("reviews").add(reviewData).addOnCompleteListener(this, OnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Review added", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ReviewsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    val errorMessage = it.exception?.message
                    Toast.makeText(this, "Failed. $errorMessage", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}