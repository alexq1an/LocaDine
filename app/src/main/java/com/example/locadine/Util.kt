package com.example.locadine

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import android.os.Build

object Util {

    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) {
            return
        }
        val permissionsToRequest = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
        val permissionsNotGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(activity!!, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsNotGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity!!,
                permissionsToRequest,
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