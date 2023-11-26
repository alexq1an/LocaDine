package com.example.locadine

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, MapViewModel.LocationCallBack,
    GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var locationManager: LocationManager
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

        // Permission check
        Util.getGPSPermission(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findRestaurantButton = binding.findButton
        findRestaurantButton.setOnClickListener(){
            // TODO future find service
            //For testing the code below should only be called when necessary
            if (currLocation != null) {
                getRoute(currLocation!!, LatLng(49.11315449002092, -122.66671572319954))
            }
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
        //initLocationManager() // initialize map
        getCurrentLocation()

        
        
        // Below is for testing function should only be called when necessary
        locationUpdates()
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Have no GPS Access ask then return
            Util.getGPSPermission(this)
            println("DBG: User Has No Permission for GPS")
            return
        }

        mapViewModel.setLocationCallBack(this)
        mapViewModel.findCurrentLocation()
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
            Util.getGPSPermission(this)
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
        if (locationManager != null)
            locationManager.removeUpdates(this)
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

    override fun onMarkerClick(marker: Marker): Boolean {
        val position = marker.position
        println("dbg: clicked!! $position")
        return true
    }

}