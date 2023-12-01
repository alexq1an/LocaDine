package com.example.locadine

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.locadine.R

class RestaurantFilterDialog : DialogFragment(), DialogInterface.OnClickListener{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the AlertDialog.Builder to set the custom view
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_restaurant_filter, null)

        builder.setView(view).setTitle("Restaurant Filter")
        builder.setPositiveButton("Apply", this)
        builder.setNegativeButton("Go Back", this)

        return builder.create()
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            Toast.makeText(activity, "Applied Filters", Toast.LENGTH_LONG).show()
        }

        if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "Back to Map", Toast.LENGTH_LONG).show()
        }
    }
}