package com.example.locadine.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.locadine.R
import com.example.locadine.pojos.GoogleReview
import com.google.type.Date
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class GoogleReviewListAdapter(private val context: Context, private val reviewList: List<GoogleReview>) :
    RecyclerView.Adapter<GoogleReviewListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val author: TextView = itemView.findViewById(R.id.author_name)
        val reviewContent: TextView = itemView.findViewById(R.id.review_content)
        val rating:RatingBar = itemView.findViewById(R.id.ratingBar)
        val date:TextView = itemView.findViewById(R.id.review_date)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.google_review_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewList[position]
        holder.author.text = "Author: ${review.author_name}"
        holder.reviewContent.text = "Review: ${review.text}"
        holder.rating.rating = review.rating.toFloat()
        holder.date.text = "Date: ${review.relative_time_description}"

    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}
