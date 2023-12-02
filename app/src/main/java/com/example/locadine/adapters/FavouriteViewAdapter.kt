package com.example.locadine.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.locadine.R

class FavouriteViewAdapter(private val context: Context, private val dataSet: ArrayList<ArrayList<String>>): RecyclerView.Adapter<FavouriteViewAdapter.ViewHolder>() {
    private lateinit var clickListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(data: ArrayList<String>)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        clickListener = listener
    }
    class ViewHolder(view: View, listener: onItemClickListener): RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val name: TextView = view.findViewById(R.id.restaurant_name_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.favourite_item, parent, false)
        return ViewHolder(view, clickListener)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataSet[position]
        println("dbg: the data $data")
        Glide.with(context)
            .load(data[1])
            .into(holder.imageView)
        holder.name.text = data[0]

        holder.itemView.setOnClickListener() {
            clickListener.onItemClick(data)
        }
    }
}