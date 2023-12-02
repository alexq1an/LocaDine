package com.example.locadine.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.locadine.R
import com.example.locadine.Util
import com.example.locadine.pojos.RestaurantInfo
import org.w3c.dom.Text

class RestaurantListAdapter(private val context: Context, private var restaurantInfo: List<RestaurantInfo>): BaseAdapter() {
    override fun getCount(): Int {
        return restaurantInfo.size
    }

    override fun getItem(position: Int): Any {
        return restaurantInfo.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.adapter_restaurant_list, null)

        val adapterImage = view.findViewById<ImageView>(R.id.adapter_image_box)
        val adapterName = view.findViewById<TextView>(R.id.adapter_restaurant_name)
        val adapterRating = view.findViewById<TextView>(R.id.adapter_restaurant_rating)
        val adapterPrice = view.findViewById<TextView>(R.id.adapter_restaurant_price)
        val adapterOpen = view.findViewById<TextView>(R.id.adapter_restaurant_opening)

        val photoUrl = Util.getPhotoUrl(restaurantInfo.get(position).photos!![0].photo_reference)
        Glide.with(view)
            .load(photoUrl)
            .into(adapterImage) // load photo into the image box

        adapterName.text = restaurantInfo.get(position).name
        adapterRating.text = "Rating: ${restaurantInfo.get(position).rating.toString()}"
        adapterPrice.text = "Price range: ${restaurantInfo.get(position).price_level.toString()}"
        adapterOpen.text = "Opening Hour: ${restaurantInfo.get(position).opening_hours.toString()}"
        // adapterDistance.text = restaurantInfo.get(position).








        return view
    }

}