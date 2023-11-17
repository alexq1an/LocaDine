package com.example.locadine.ViewModels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class MapViewModel(private val fusedLocationProviderClient: FusedLocationProviderClient): ViewModel() {
    private var currLocation: LatLng = LatLng(0.0,0.0)
    private lateinit var locationCallBack: LocationCallBack

    fun setLocationCallBack(callBack: LocationCallBack) {
        this.locationCallBack = callBack
    }

    @SuppressLint("MissingPermission")
    fun findCurrentLocation() {
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener {
            currLocation = LatLng(it.latitude, it.longitude)
            locationCallBack.onLocationGet(currLocation)
        }.addOnFailureListener {
            println(it)
        }
    }

    interface LocationCallBack {
        fun onLocationGet(location: LatLng)
    }


}