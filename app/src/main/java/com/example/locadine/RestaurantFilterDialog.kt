package com.example.locadine

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class RestaurantFilterDialog : DialogFragment(), DialogInterface.OnClickListener{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the AlertDialog.Builder to set the custom view
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_restaurant_filter, null)

        builder.setView(view).setTitle("Restaurant Filter")
        builder.setPositiveButton("Apply", this)




        val distanceSpinner = view.findViewById<Spinner>(R.id.filter_spinner_distance)
        val ratingSpinner = view.findViewById<Spinner>(R.id.filter_spinner_rating)
        val priceSpinner = view.findViewById<Spinner>(R.id.filter_spinner_price)

        // Load filter settings from SharedPreferences
        distanceSpinner.setSelection(getSavedDistance(FilterSetting.distance, R.array.filter_distance))
        ratingSpinner.setSelection(getSavedRating(FilterSetting.rating, R.array.filter_rating))
        priceSpinner.setSelection(getSavedPrice(FilterSetting.price, R.array.filter_price))

        distanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {

                val selectedDistance = resources.getStringArray(R.array.filter_distance)[position]
                // pass selected distance to the activity
                FilterSetting.distance = when (selectedDistance){
                    "< 200m" -> 200
                    "< 500m" -> 500
                    "< 1000m" -> 1000
                    "< 5000m" -> 5000
                    else -> 200
                }

            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }

        ratingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {

                val selectedDistance = resources.getStringArray(R.array.filter_rating)[position]
                // pass selected rating to the activity
                FilterSetting.rating = when (selectedDistance){
                    "0" -> 0.0
                    "> 3.5" -> 3.5
                    "> 4.0" -> 4.0
                    "> 4.5" -> 4.5
                    else -> 0.0
                }

            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }

        priceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {

                val selectedDistance = resources.getStringArray(R.array.filter_price)[position]
                // pass selected price to the activity
                FilterSetting.price = when (selectedDistance){
                    "$" -> 0
                    "$$" -> 1
                    "$$$" -> 2
                    "$$$$" -> 3
                    "$$$$$" -> 4
                    else -> 1
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        FilterSetting.updateTrigger = true // set it true for update later
    }


    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {

            Toast.makeText(activity, "Applied Filters", Toast.LENGTH_LONG).show()
        }

    }

    private fun getSavedDistance(value: Int, arrayId: Int): Int {
        val filterArray = resources.getStringArray(arrayId)
        val stringValue = when (value) {
            200 -> "< 200m"
            500 -> "< 500m"
            1000 -> "< 1000m"
            5000 -> "< 5000m"
            else -> "< 200m"
        }
        return filterArray.indexOf(stringValue)
    }

    private fun getSavedRating(value: Double, arrayId: Int): Int {
        val filterArray = resources.getStringArray(arrayId)
        val stringValue = when (value) {
            0.0 -> "default"
            3.5 -> "> 3.5"
            4.0 -> "> 4.0"
            4.5 -> "> 4.5"
            else -> "> 3.5"
        }
        return filterArray.indexOf(stringValue)
    }

    private fun getSavedPrice(value: Int, arrayId: Int): Int {
        val filterArray = resources.getStringArray(arrayId)
        val stringValue = when (value) {
            0 -> "$"
            1 -> "$$"
            2 -> "$$$"
            3 -> "$$$$"
            4 -> "$$$$$"
            else -> 20.0
        }
        return filterArray.indexOf(stringValue)
    }


}