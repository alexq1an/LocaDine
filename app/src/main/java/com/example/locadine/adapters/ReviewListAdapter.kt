package com.example.locadine.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.locadine.R
import com.example.locadine.pojos.Review

class ReviewListAdapter(private val context: Context, private var reviewList: List<Review>): BaseAdapter(){
    override fun getCount(): Int {
        return reviewList.size
    }

    override fun getItem(position: Int): Any {
        return reviewList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.review_item, null)
        val restaurantName = view.findViewById<TextView>(R.id.review_restaurant_name)
        val reviewContent = view.findViewById<TextView>(R.id.review_content)
        val rating = view.findViewById<TextView>(R.id.review_rating)
        val reviewerEmail = view.findViewById<TextView>(R.id.reviewer_email)
        val createdAt = view.findViewById<TextView>(R.id.review_createdAt)

        val review = reviewList[position]
        restaurantName.text = "Restaurant name: ${review.restaurantName}"
        reviewContent.text = "Review: ${review.review}"
        rating.text = "Rating: ${review.rating}/5"
        reviewerEmail.text = "Reviewer: ${review.reviewerEmail}"
        createdAt.text = "Created at: ${review.createdAt}"

        return view
    }

    fun replace(newList: List<Review>) {
        reviewList = newList
    }
}