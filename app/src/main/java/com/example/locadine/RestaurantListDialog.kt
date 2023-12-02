package com.example.locadine

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.locadine.adapters.RestaurantListAdapter
import com.example.locadine.pojos.RestaurantInfo

class RestaurantListDialog: DialogFragment(), DialogInterface.OnClickListener{

    private lateinit var restaurantListView : ListView
    private lateinit var restaurantList : List<RestaurantInfo>
    private lateinit var arrayAdapter: RestaurantListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_restaurant_list, null)

        builder.setView(view).setTitle("Restaurant List:")
        builder.setNegativeButton("Back", this)

        restaurantListView = view.findViewById(R.id.restaurant_list)
        restaurantList = ArrayList()
        try{
            restaurantList = FilterSetting.restaurants!!
        }catch(e:Exception){ // do not transfer value to the list if user haven't find their restaurant yet
            if (FilterSetting.restaurants == null)
                Toast.makeText(requireContext(),"There is no restaurant can show, please find your restaurant first",Toast.LENGTH_SHORT).show()
        }
        arrayAdapter = RestaurantListAdapter(requireContext(), restaurantList)
        restaurantListView.adapter = arrayAdapter

        return builder.create()
    }
    override fun onClick(p0: DialogInterface?, p1: Int) {
    }
}