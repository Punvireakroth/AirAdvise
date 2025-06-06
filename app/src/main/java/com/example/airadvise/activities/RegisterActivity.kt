package com.example.airadvise.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.R
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.ActivityRegisterBinding
import com.example.airadvise.models.request.RegisterRequest
import com.example.airadvise.utils.Validator
import com.example.airadvise.utils.Resource
import com.example.airadvise.utils.safeApiCall
import com.facebook.stetho.Stetho
import kotlinx.coroutines.launch

class RegisterActivity: AppCompatActivity() {
    public lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Stetho.initializeWithDefaults(this)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        setupTextWatchers()

        // If user click on I already have an account text, go to LoginActivity
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupTextWatchers() {
        // Password field text watcher to show strength indicator
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isNotEmpty()) {
                    binding.tvPasswordStrength.visibility = View.VISIBLE

                    when {
                        Validator.isStrongPassword(password) -> {
                            binding.tvPasswordStrength.text = getString(R.string.password_strong)
                            binding.tvPasswordStrength.setTextColor(getColor(R.color.aqi_good))
                        }
                        Validator.isValidPassword(password) -> {
                            binding.tvPasswordStrength.text = getString(R.string.password_medium)
                            binding.tvPasswordStrength.setTextColor(getColor(R.color.aqi_moderate))
                        }
                        else -> {
                            binding.tvPasswordStrength.text = getString(R.string.password_weak)
                            binding.tvPasswordStrength.setTextColor(getColor(R.color.aqi_unhealthy))
                        }
                    }
                } else {
                    binding.tvPasswordStrength.visibility = View.GONE
                }
            }
        })
    }

    private fun setupClickListeners() {
        // Register button click
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        // Already have account text click
        binding.tvLogin.setOnClickListener {
            finish()  // Go back to login screen
        }
    }

    private fun performRegistration() {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Create registration request
        val registerRequest = RegisterRequest(
            name = name,
            email = email,
            password = password,
            password_confirmation = confirmPassword
        )

        // Use coroutine for network call
        lifecycleScope.launch {
            // Using safeApiCall utility
            val result = safeApiCall { ApiClient.createApiService(this@RegisterActivity).register(registerRequest) }
            
            when (result) {
                is Resource.Success -> {
                    val authResponse = result.data!!
                    
                    // Show success message
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.registration_successful),
                        Toast.LENGTH_LONG
                    ).show()

                    
                    // Navigate to LoginActivity
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Close current activity (RegisterActivity)
                }
                is Resource.Error -> {
                    // Parse error message
                    val errorMessage = when {
                        result.message?.contains("409") == true -> getString(R.string.email_already_exists)
                        result.message?.contains("422") == true -> getString(R.string.validation_error)
                        result.message?.contains("network") == true -> getString(R.string.network_error)
                        else -> result.message ?: getString(R.string.registration_failed)
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    // This state isn't used in the current implementation
                }
            }
            
            // Hide loading indicator
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val agreeToTerms = binding.cbTerms.isChecked

        // Reset error states
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validate name
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.email_required)
            return false
        }

        if (!Validator.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            return false
        }

        if (!Validator.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            return false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.confirm_password_required)
            return false
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.passwords_dont_match)
            return false
        }

        // Validate terms agreement
        if (!agreeToTerms) {
            Toast.makeText(this, getString(R.string.please_agree_to_terms), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}