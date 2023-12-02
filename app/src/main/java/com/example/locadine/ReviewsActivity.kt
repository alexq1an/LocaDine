package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.example.locadine.adapters.ReviewListAdapter
import com.example.locadine.pojos.Review
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore

class ReviewsActivity : AppCompatActivity() {
    private lateinit var addReviewButton: Button
    private lateinit var arrayList: ArrayList<Review>
    private lateinit var arrayAdapter: ReviewListAdapter

    private lateinit var reviewListView: ListView

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        reviewListView = findViewById(R.id.review_list)

        arrayList = ArrayList()
        arrayAdapter = ReviewListAdapter(this, arrayList)
        reviewListView.adapter = arrayAdapter

        db = FirebaseFirestore.getInstance()

        addReviewButton = findViewById(R.id.add_review_button)
        addReviewButton.setOnClickListener {
            startActivity(Intent(this, AddReviewActivity::class.java))
        }

        fetchReviews()
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