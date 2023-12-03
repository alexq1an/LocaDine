package com.example.locadine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.locadine.ViewModels.MapViewModel
import com.google.android.gms.maps.model.LatLng
import com.example.locadine.services.NotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity(), MapViewModel.LocationCallBack {
    private lateinit var logoutButton: Button
    private lateinit var userInfo: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var mapButton: Button
    private lateinit var favouriteButton: Button

    private val CHANNEL_ID = "localdine"
    private val CHANNEL_NAME = "Loca Dine"
    private val CHANNEL_DESC = "Loca Dine Notifications"
    private val FIREBASE_NOTIFICATIONS_TOPIC = "announcements"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Util.checkPermissions(this)

        setupNotification()

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.main_logout_button)
        userInfo = findViewById(R.id.user_info)
        mapButton = findViewById(R.id.map_button)
        favouriteButton = findViewById(R.id.favourite_restaurant_button)

        val isLoggedIn = auth.currentUser != null
        if (isLoggedIn) {
            userInfo.text = "Welcome to Locadine\n${auth.currentUser!!.email}"
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        mapButton.setOnClickListener(){
            startActivity(Intent(this, MapsActivity::class.java))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        favouriteButton.setOnClickListener {
            startActivity(Intent(this, MyFavourites::class.java))
        }
    }

    private fun setupNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_DESC
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_NOTIFICATIONS_TOPIC)
    }

    override fun onLocationGet(location: LatLng) {
        println("DBG: Current Location: $location")
    }
}

