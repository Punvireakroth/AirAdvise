package com.example.airadvise.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.MainActivity
import com.example.airadvise.R
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.ActivityLoginBinding
import com.example.airadvise.models.request.LoginRequest
import com.example.airadvise.utils.Validator
import com.example.airadvise.utils.SessionManager
import com.example.airadvise.utils.Resource
import com.example.airadvise.utils.safeApiCall
import com.facebook.stetho.Stetho
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Stetho.initializeWithDefaults(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener() {
            if (validateInputs()) {
                performLogin()
            }
        }

        // Register text click
        binding.tvRegister.setOnClickListener {
            // Navigate to register screen
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Forgot password text click
        binding.tvForgotPassword.setOnClickListener {
            // For now, we just show a toast
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val rememberMe = binding.cbRememberMe.isChecked

        // Create login request
        val loginRequest = LoginRequest(email, password)

        // Use coroutine for network call
        lifecycleScope.launch {
            // Using safeApiCall utility
            val result = safeApiCall { ApiClient.createApiService(this@LoginActivity).login(loginRequest) }
            
            when (result) {
                is Resource.Success -> {
                    val authResponse = result.data!!
                    
                    // Store authentication token and user data
                    SessionManager.saveAuthToken(this@LoginActivity, authResponse.token)
                    SessionManager.saveUserId(this@LoginActivity, authResponse.user.id)
                    SessionManager.saveUserData(this@LoginActivity, authResponse.user)
                    SessionManager.setRememberMe(this@LoginActivity, rememberMe)
                    
                    // Navigate to MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    // Parse error message
                    val errorMessage = when {
                        result.message?.contains("401") == true -> getString(R.string.invalid_credentials)
                        result.message?.contains("network") == true -> getString(R.string.network_error)
                        else -> getString(R.string.login_failed)
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    // This state isn't used in the current implementation
                }
            }
            
            // Hide loading indicator
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Reset error states
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validate email
        if (!Validator.isNotEmpty(email)) {
            binding.tilEmail.error = getString(R.string.email_required)
            return false
        }

        if (!Validator.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            return false
        }

        // Validate password
        if (!Validator.isNotEmpty(password)) {
            binding.tilPassword.error = getString(R.string.password_required)
            return false
        }

        return true
    }
}