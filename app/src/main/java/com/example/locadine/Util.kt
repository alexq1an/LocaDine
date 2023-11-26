package com.example.locadine

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
object Util {


    fun getGPSPermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    fun getRoutingUrl(start: LatLng, dest: LatLng): String {

        val url = "https://maps.googleapis.com/maps/api/directions/json"
        val destString = "destination=${dest.latitude},${dest.longitude}"
        val startString = "origin=${start.latitude},${start.longitude}"
        val apiKey = "key="

        return "$url?$destString&$startString&$apiKey"
    }
}