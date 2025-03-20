package es.uc3m.android.stride.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.uc3m.android.stride.R
import es.uc3m.android.stride.databinding.ActivityLoginBinding
import es.uc3m.android.stride.ui.TrackingActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    // registration activity
    private fun setupClickListeners() {
        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        // login button
        binding.btnLogin.setOnClickListener {
            if (validateForm()) {
                loginUser()
            }
        }

        // forgot password
        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(
                this,
                getString(R.string.forgot_password_coming_soon),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // email and password validation
    private fun validateForm(): Boolean {
        var isValid = true

        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        val password = binding.etPassword.text.toString()
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    // hardcoded login credentials yee
    private fun loginUser() {
        val hardcodedEmail = "user@example.com"
        val hardcodedPassword = "password"

        val enteredEmail = binding.etEmail.text.toString().trim()
        val enteredPassword = binding.etPassword.text.toString()

        if (enteredEmail == hardcodedEmail && enteredPassword == hardcodedPassword) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show()
        }

        // remember me button - doesn't work yet lol
        if (binding.cbRememberMe.isChecked) {
            // TODO: Store credentials securely or set a flag in SharedPreferences
        }
    }
}