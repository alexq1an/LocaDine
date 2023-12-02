package com.example.locadine.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.locadine.R
import com.example.locadine.pojos.RestaurantInfo

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




        return view
    }

}