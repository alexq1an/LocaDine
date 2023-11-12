package com.example.locadine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var signupLink: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.login_email)
        password = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_login_button)
        signupLink = findViewById(R.id.signup_link)

        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            if (emailText.isBlank()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (passwordText.isBlank()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                login(emailText, passwordText)
            }
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                val errorMessage = it.exception?.message
                Toast.makeText(this, "Failed to login. $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }
}