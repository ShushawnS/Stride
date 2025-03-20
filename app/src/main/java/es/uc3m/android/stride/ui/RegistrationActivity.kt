package es.uc3m.android.stride.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.uc3m.android.stride.R
import es.uc3m.android.stride.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login link click listener
        binding.tvLoginLink.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close registration activity
        }

        // Register button click listener
        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate name
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // Validate email
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

        // Validate password
        val password = binding.etPassword.text.toString()
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // Validate confirm password
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_required)
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        // Validate terms checkbox
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(
                this,
                getString(R.string.error_terms_required),
                Toast.LENGTH_SHORT
            ).show()
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        // Here you would implement the actual registration logic
        // For now, we'll just show a success message and navigate to the next screen

        // Show loading state if needed
        // binding.btnRegister.isEnabled = false
        // binding.progressBar.visibility = View.VISIBLE

        // TODO: Add your registration API call or database operation here

        // For demo purposes, we'll just show a success message and navigate
        Toast.makeText(
            this,
            getString(R.string.registration_success),
            Toast.LENGTH_SHORT
        ).show()

        // Navigate to the main activity or onboarding
        // val intent = Intent(this, MainActivity::class.java)
        // startActivity(intent)
        // finish()
    }
}