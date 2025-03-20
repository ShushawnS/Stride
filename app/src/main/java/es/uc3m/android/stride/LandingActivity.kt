package es.uc3m.android.stride.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import es.uc3m.android.stride.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registration screen logic
        binding.btnGetStarted.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        // Login activity
        binding.tvLoginPrompt.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}