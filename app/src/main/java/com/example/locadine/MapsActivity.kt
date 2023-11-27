package com.example.locadine

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.example.locadine.ViewModels.MapViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.locadine.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, MapViewModel.LocationCallBack,
    GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var findRestaurantButton: Button
    private lateinit var mapSwitch: Button
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapViewModel: MapViewModel

    private var polyLine: Polyline? = null
    private var currLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findRestaurantButton = binding.findButton
        findRestaurantButton.setOnClickListener(){
            // TODO future find service
        }

        mapSwitch = binding.mapSwitch
        mapSwitch.setOnClickListener(){
            if(mMap.mapType == GoogleMap.MAP_TYPE_NORMAL){
                mMap.mapType= GoogleMap.MAP_TYPE_SATELLITE
            }else{
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }

        }

        mapViewModel = MapViewModel(fusedLocationProviderClient)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (mMap.mapType == null) {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        markerOptions = MarkerOptions()
        polylineOptions = PolylineOptions()
        
        mMap.setOnMarkerClickListener(this)
        getCurrentLocation()
    }

    //gets the current location of device Once
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("DBG: User Has No Permission for GPS")
            return
        }

        mapViewModel.setLocationCallBack(this)
        mapViewModel.findCurrentLocation()
    }


    // initialize marker for each iteration
    private var lastMarker: Marker? = null

    // This Tracks the device in intervals and marks the location on the map
    fun locationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Have no GPS Access ask then return
            println("DBG: User Has No Permission for GPS")
            return
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        mapViewModel.getLocationUpdates(locationRequest)
        mapViewModel.locationLiveData.observe(this) { location ->
            println("dbg: Update $location")
            val position = LatLng(location.latitude , location.longitude)
            lastMarker?.remove()
            markerOptions.position(position).title("You are here")
            lastMarker = mMap.addMarker(markerOptions)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 17f)
            mMap.animateCamera(cameraUpdate)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fusedLocationProviderClient != null) {
            // Stops tracking the device
            mapViewModel.stopLocationUpdates()
        }
    }

    override fun onLocationGet(location: LatLng) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 17f)
        mMap.animateCamera(cameraUpdate)
        println("dbg: Current $location")
        lastMarker?.remove()
        markerOptions.position(location).title("You Are Here")
        lastMarker = mMap.addMarker(markerOptions)
        currLocation = location
    }


    // Takes a starting LatLng and destination LatLng then displays the route on the map
    private fun getRoute(start: LatLng, destination: LatLng) {
        // Can show How long to get to destination in minutes and distance
        val url = Util.getRoutingUrl(
            start,
            destination
        )

        mapViewModel.getRoutes(url)
        val route = ArrayList<LatLng>()
        var duration = ""
        var distance = ""
        mapViewModel.routeLiveData.observe(this) {

            it.routes[0].legs[0].steps.forEach { i ->
                route.addAll(mapViewModel.decodePolyline(i.polyline.points))
            }
            duration = it.routes[0].legs[0].duration.text
            distance = it.routes[0].legs[0].distance.text
            if (polyLine != null) {
                polyLine!!.remove()
            }
            polylineOptions.addAll(route)
            polylineOptions.color(Color.BLUE).width(20F)
            polyLine = mMap.addPolyline(polylineOptions)

            println("dbg: A Duration: $duration A Distance: $distance")
        }
    }

    //Returns the LatLng of the clicked marker, can change to do something else
    override fun onMarkerClick(marker: Marker): Boolean {
        val position = marker.position
        println("dbg: clicked!! $position")
        return true
    }

}