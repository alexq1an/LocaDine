package com.example.locadine

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.locadine.ViewModels.MapViewModel
import com.example.locadine.api.GooglePlacesAPIService

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.locadine.databinding.ActivityMapsBinding
import com.example.locadine.pojos.NearbySearchResponse
import com.example.locadine.pojos.RestaurantInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, MapViewModel.LocationCallBack {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var mapSwitch: Button
    private lateinit var restaurantList: Button
    private lateinit var findButton: Button
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapViewModel: MapViewModel

    private var polyLine: Polyline? = null
    private var currLocation: LatLng? = null

    private lateinit var googlePlacesAPIService: GooglePlacesAPIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Activate Google Place API
        val retrofit = Util.getGooglePlacesRetrofitInstance()
        googlePlacesAPIService = retrofit.create(GooglePlacesAPIService::class.java)


        fetchRestaurants() // fetch restaurant when user enters the app

        findButton = binding.findButton
        findButton.setOnClickListener() {
            fetchRestaurants()
            mapSwitch = binding.mapSwitch
            mapSwitch.setOnClickListener() {
                if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                } else {
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                }

            }

            restaurantList = binding.listButton
            restaurantList.setOnClickListener() {
                val restaurantListDialog = RestaurantListDialog()
                val bundle = Bundle()
                restaurantListDialog.show(supportFragmentManager, "RestaurantList")
            }

            // for filter restaurant
            val toolbarButton = findViewById<Button>(R.id.restaurant_filter)
            toolbarButton.setOnClickListener {
                val filterDialog = RestaurantFilterDialog()
                val bundle = Bundle()
                filterDialog.show(supportFragmentManager, "RestaurantFilter")


            }

            mapViewModel = MapViewModel(fusedLocationProviderClient)
        }
    }


    @SuppressLint("MissingPermission")
    fun requestLocation(callback: (latitude: Double, longitude: Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Call the callback with the location
                    callback(latitude, longitude)
                }
            }
    }

    private fun fetchRestaurants() {

        // Fetch the user's location
        requestLocation { latitude, longitude ->

            // Now that we have the user's location, proceed to fetch nearby restaurants
            val radiusInMeters = FilterSetting.distance
            val curLocation = "$latitude,$longitude"

            // Clear existing markers
            mMap.clear()

            // Fetch nearby restaurants
            val call = googlePlacesAPIService.findNearbyRestaurants(curLocation, radiusInMeters, BuildConfig.MAPS_API_KEY)
            call.enqueue(object : Callback<NearbySearchResponse> {
                override fun onResponse(
                    call: Call<NearbySearchResponse>,
                    response: Response<NearbySearchResponse>
                ) {
                    if (response.isSuccessful) {
                        val restaurants = response.body()!!.results

                        // Apply filters
                        val filterRatingLayer1 = filterByRating(restaurants, FilterSetting.rating) // apply filter layer by layer
                         val filterPriceLayer2 = filterByPrice(filterRatingLayer1, FilterSetting.price) // next layer


                        val markerList = mutableListOf<Marker>() // create a list for marker to calculate camera bound
                        // Iterate through the list of restaurants and add markers
                        filterPriceLayer2?.forEach { filterPriceLayer2 -> // for each restaurant after filter
                            val restaurantLocation = filterPriceLayer2.geometry.location
                            val restaurantLatLng = LatLng(restaurantLocation.lat, restaurantLocation.lng)

                            val marker = mMap.addMarker(
                                MarkerOptions()
                                    .position(restaurantLatLng)
                                    .title("${filterPriceLayer2.rating}  ${filterPriceLayer2.name}")

                            )
                            if (marker != null) { // Mandatory not null check to add marker to the list
                                markerList.add(marker)
                            }
                        }

                        FilterSetting.restaurants = filterPriceLayer2 // pass all the data fetched into object for list data

                        try{
                            moveCameraToFitMarkers(markerList)
                        } catch(e:Exception){
                            Toast.makeText(applicationContext, "There is no desired restaurant with current filter setting in your region",Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Toast.makeText(applicationContext, "Problem with Showing Markers", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NearbySearchResponse>, t: Throwable) {
                    // Handle the failure case, such as a network error
                }
            })
        }
    }

    private fun filterByRating(restaurants: List<RestaurantInfo>, minRating: Double): List<RestaurantInfo> {
        return restaurants.filter { it.rating?.let { rating -> rating > minRating } ?: false }
    }

    private fun filterByPrice(restaurants: List<RestaurantInfo>, priceLevel: Int): List<RestaurantInfo> {
        return restaurants.filter { it.price_level?.let { level -> level <= priceLevel } ?: false }
    }


    private fun moveCameraToFitMarkers(markerList: List<Marker>) {
        val builder = LatLngBounds.Builder()

        for (marker in markerList) {
            builder.include(marker.position)
        }

        val bounds = builder.build()
        val padding = 100 // Adjust the padding as needed

        // Move camera to fit the markers on the map
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.moveCamera(cu)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (mMap.mapType == null) {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        markerOptions = MarkerOptions()
        polylineOptions = PolylineOptions()

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

        navigationCheck(location)
    }

    private fun navigationCheck(location: LatLng) {
        val intentValues = intent.getBooleanExtra("Navigate", false)
        if (intentValues) {
            val lat = intent.getDoubleExtra("Lat", 0.0)
            val lng = intent.getDoubleExtra("Lng", 0.0)
            getRoute(location, LatLng(lat,lng))
            locationUpdates()
        }

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
}