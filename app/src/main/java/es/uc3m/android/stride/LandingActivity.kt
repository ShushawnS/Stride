package es.uc3m.android.stride.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import es.uc3m.android.stride.databinding.ActivityLandingBinding

import es.uc3m.android.stride.ui.RegistrationActivity
import es.uc3m.android.stride.ui.LoginActivity

class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up button click listeners
        binding.btnGetStarted.setOnClickListener {
            // Navigate to registration screen
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        binding.tvLoginPrompt.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}