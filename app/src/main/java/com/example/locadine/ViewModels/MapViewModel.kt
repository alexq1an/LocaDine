package com.example.locadine.ViewModels

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.locadine.pojos.RouteData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MapViewModel(private val fusedLocationProviderClient: FusedLocationProviderClient): ViewModel() {
    private var currLocation: LatLng = LatLng(0.0,0.0)
    private lateinit var locationCallBack: LocationCallBack
    private lateinit var locationUpdateCallback: LocationCallback
    private val locationMutLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> get() = locationMutLiveData
    private val routeMutLiveData = MutableLiveData<RouteData>()
    val routeLiveData: LiveData<RouteData> get() = routeMutLiveData


    fun setLocationCallBack(callback: LocationCallBack) {
        this.locationCallBack = callback
    }


    interface LocationCallBack {
        fun onLocationGet(location: LatLng)
    }
    @SuppressLint("MissingPermission")
    fun findCurrentLocation() {
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
            currLocation = LatLng(it.latitude, it.longitude)
            locationCallBack.onLocationGet(currLocation)
        }.addOnFailureListener {
            println(it)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(locationRequest: LocationRequest){

        locationUpdateCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let {
                    locationMutLiveData.value = locationResult.lastLocation
                }
                }
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationUpdateCallback, null)
    }

    fun getRoutes(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    return
                }
                try {
                    val res = response.body?.string()

                    if (res.isNullOrBlank()) {
                        return
                    }
                    val data = Gson().fromJson(res, RouteData::class.java)
                    val route = ArrayList<LatLng>()
                    data.routes[0].legs[0].steps.forEach { i ->
                        route.addAll(decodePolyline(i.polyline.points))
                    }
                    routeMutLiveData.postValue(data)
                } catch (e: IOException) {
                    println("dbg: Result ERROR")
                    e.printStackTrace()
                }
            }
        })
    }


    // Code Help from
    // https://www.geeksforgeeks.org/how-to-generate-route-between-two-locations-in-google-map-in-android/
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

}