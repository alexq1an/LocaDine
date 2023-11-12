package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var logoutButton: Button
    private lateinit var userInfo: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.main_logout_button)
        userInfo = findViewById(R.id.user_info)

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
    }
}