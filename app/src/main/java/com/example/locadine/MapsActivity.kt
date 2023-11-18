package com.example.locadine

import android.app.Activity
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.locadine.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var  markerOptions: MarkerOptions
    private lateinit var locationManager: LocationManager
    private lateinit var findRestaurantButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Permission check
        Util.checkPermissions(this)

        findRestaurantButton = binding.findButton
        findRestaurantButton.setOnClickListener(){
            // TODO future find service


        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()

        initLocationManager() // initialize map

    }

    fun initLocationManager() {
        try { // initialize steps
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                return
            }
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null)
                onLocationChanged(location)
            // update 1 second at a time
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)

        } catch (e: SecurityException) { // catch no location permission exception
            Toast.makeText(this, "No Location permission", Toast.LENGTH_SHORT).show()
        }

    }

    // initialize marker for each iteration
    private var lastMarker: Marker? = null
    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        // update camera position
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
        mMap.animateCamera(cameraUpdate)

        lastMarker?.remove()
        markerOptions.position(latLng)
        // keep a reference to the last marker
        lastMarker = mMap.addMarker(markerOptions)


    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
    }
}