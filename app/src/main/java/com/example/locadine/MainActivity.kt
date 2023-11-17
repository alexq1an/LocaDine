package com.example.locadine

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.locadine.ViewModels.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), MapViewModel.LocationCallBack {
    private lateinit var logoutButton: Button
    private lateinit var userInfo: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var goToReviewsButton: Button

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Util.getGPSPermission(this)

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.main_logout_button)
        userInfo = findViewById(R.id.user_info)
        goToReviewsButton = findViewById(R.id.go_to_reviews)

        val isLoggedIn = auth.currentUser != null
        if (isLoggedIn) {
            userInfo.text = auth.currentUser!!.email
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        goToReviewsButton.setOnClickListener {
            startActivity(Intent(this, ReviewsActivity::class.java))
        }

        //----------------------------------------------
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapViewModel = MapViewModel(fusedLocationProviderClient)
        getCurrentLocation()
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
            return
        }
        mapViewModel.setLocationCallBack(this)
        mapViewModel.findCurrentLocation()
    }

    override fun onLocationGet(location: LatLng) {
        println("RDB: 222 $location")
    }

}