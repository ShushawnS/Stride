package es.uc3m.android.stride.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.stride.R
import es.uc3m.android.stride.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            if (validateForm()) {
                loginUser()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password.", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, TrackingActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
