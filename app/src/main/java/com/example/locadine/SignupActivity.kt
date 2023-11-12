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

class SignupActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signupButton: Button
    private lateinit var loginLink: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        email = findViewById(R.id.signup_email)
        password = findViewById(R.id.signup_password)
        signupButton = findViewById(R.id.signup_signup_button)
        loginLink = findViewById(R.id.login_link)

        auth = FirebaseAuth.getInstance()

        signupButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            if (emailText.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (passwordText.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                signup(emailText, passwordText)
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun signup(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Signed up successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                val errorMessage = it.exception?.message
                Toast.makeText(this, "Failed to signup. $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }
}