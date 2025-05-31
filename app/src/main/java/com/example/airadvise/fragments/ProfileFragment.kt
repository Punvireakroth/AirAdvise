package com.example.airadvise.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.R
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.FragmentProfileBinding
import com.example.airadvise.models.User
import com.example.airadvise.models.UserPreferences
import com.example.airadvise.models.request.ChangePasswordRequest
import com.example.airadvise.utils.PreferenceManager
import com.example.airadvise.utils.Resource
import com.example.airadvise.utils.SessionManager
import com.example.airadvise.utils.safeApiCall
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var preferenceManager: PreferenceManager
    private var currentUser: User? = null
    private var userPreferences: UserPreferences? = null

    // Language options
    private val languageOptions = arrayOf("English", "Spanish", "French", "German", "Chinese")
    private val languageCodes = arrayOf("en", "es", "fr", "de", "zh")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        sessionManager = SessionManager(requireContext())
        preferenceManager = PreferenceManager.getInstance(requireContext())

        setupLanguageSpinner()
        setupThemeRadioGroup()
        setupAqiThresholdSeekBar()
        setupNameEditing()
        setupButtons()

        // Load user data and preferences
        loadUserData()
        loadUserPreferences()
    }

    private fun setupLanguageSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languageOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Store selected language
                preferenceManager.saveLanguagePreference(languageCodes[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupThemeRadioGroup() {
        // Set initial selection based on saved preference
        val themeMode = preferenceManager.getThemeMode()
        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.lightThemeRadio.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.darkThemeRadio.isChecked = true
            else -> binding.systemThemeRadio.isChecked = true
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newThemeMode = when (checkedId) {
                R.id.lightThemeRadio -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.darkThemeRadio -> AppCompatDelegate.MODE_NIGHT_YES
                R.id.systemThemeRadio -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            preferenceManager.saveThemeMode(newThemeMode)
        }
    }

    private fun setupAqiThresholdSeekBar() {
        // Set initial threshold from preferences
        val aqiThreshold = preferenceManager.getAqiThreshold()
        binding.aqiThresholdSeekBar.progress = aqiThreshold
        updateAqiThresholdLabel(aqiThreshold)

        binding.aqiThresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateAqiThresholdLabel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Save the new threshold
                seekBar?.progress?.let { preferenceManager.saveAqiThreshold(it) }
            }
        })
    }

    private fun updateAqiThresholdLabel(threshold: Int) {
        binding.aqiThresholdLabel.text = "Alert me when AQI exceeds: $threshold"
    }

    private fun setupNameEditing() {
        binding.nameEditText.isEnabled = false

        binding.editNameButton.setOnClickListener {
            binding.nameEditText.isEnabled = true
            binding.nameEditText.requestFocus()
            binding.saveNameButton.visibility = View.VISIBLE
            binding.editNameButton.visibility = View.GONE
        }

        binding.saveNameButton.setOnClickListener {
            val newName = binding.nameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateUserName(newName)
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        binding.savePreferencesButton.setOnClickListener {
            saveUserPreferences()
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Set notification switch initial states
        binding.notificationsSwitch.isChecked = preferenceManager.getNotificationsEnabled()
        binding.dailyForecastSwitch.isChecked = preferenceManager.getDailyForecastEnabled()

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.saveNotificationsEnabled(isChecked)
            if (!isChecked) {
                binding.dailyForecastSwitch.isChecked = false
                preferenceManager.saveDailyForecastEnabled(false)
            }
        }

        binding.dailyForecastSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (binding.notificationsSwitch.isChecked) {
                preferenceManager.saveDailyForecastEnabled(isChecked)
            } else if (isChecked) {
                binding.notificationsSwitch.isChecked = true
                preferenceManager.saveNotificationsEnabled(true)
                preferenceManager.saveDailyForecastEnabled(true)
            }
        }
    }

    private fun loadUserData() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).getCurrentUser()
                }

                when (response) {
                    is Resource.Success -> {
                        currentUser = response.data
                        updateUiWithUserData(currentUser)
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error loading user data: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Loading -> {
                        // Already showing loading
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loadUserPreferences() {
        lifecycleScope.launch {
            try {
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).getUserPreferences()
                }

                when (response) {
                    is Resource.Success -> {
                        userPreferences = response.data
                        updateUiWithPreferences(userPreferences)
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error loading preferences: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Loading -> {
                        // Handle loading state
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateUiWithUserData(user: User?) {
        user?.let {
            binding.emailValueText.text = it.email
            binding.nameEditText.setText(it.name)
        }
    }

    private fun updateUiWithPreferences(preferences: UserPreferences?) {
        preferences?.let {
            // Update UI based on server preferences
            binding.notificationsSwitch.isChecked = it.notificationsEnabled
            binding.dailyForecastSwitch.isChecked = it.dailyForecastEnabled
            
            // Update AQI threshold
            val aqiThreshold = it.aqiThreshold ?: 100
            binding.aqiThresholdSeekBar.progress = aqiThreshold
            updateAqiThresholdLabel(aqiThreshold)
            
            // Update language selection
            val languageIndex = languageCodes.indexOf(it.language)
            if (languageIndex >= 0) {
                binding.languageSpinner.setSelection(languageIndex)
            }
        }
    }

    private fun updateUserName(newName: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val updatedUser = currentUser?.copy(name = newName) ?: return@launch

                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).updateUser(updatedUser)
                }

                when (response) {
                    is Resource.Success -> {
                        currentUser = response.data
                        binding.nameEditText.isEnabled = false
                        binding.saveNameButton.visibility = View.GONE
                        binding.editNameButton.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error updating name: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Loading -> {
                        // Already showing loading
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveUserPreferences() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val updatedPreferences = userPreferences?.copy(
                    notificationsEnabled = binding.notificationsSwitch.isChecked,
                    dailyForecastEnabled = binding.dailyForecastSwitch.isChecked,
                    aqiThreshold = binding.aqiThresholdSeekBar.progress,
                    language = languageCodes[binding.languageSpinner.selectedItemPosition],
                    darkMode = when {
                        binding.lightThemeRadio.isChecked -> false
                        binding.darkThemeRadio.isChecked -> true
                        else -> null // System default
                    }
                ) ?: return@launch

                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).updateUserPreferences(updatedPreferences)
                }

                when (response) {
                    is Resource.Success -> {
                        userPreferences = response.data
                        Toast.makeText(requireContext(), "Preferences saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error saving preferences: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Loading -> {
                        // Already showing loading
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).logout()
                }

                // Regardless of response, clear session
//                sessionManager.clearSession()

                // Navigate to login screen
                requireActivity().finish()
                // Start LoginActivity
                startActivity(android.content.Intent(requireContext(), com.example.airadvise.activities.LoginActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error during logout: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Still clear session locally
//                sessionManager.clearSession()
                
                // Navigate to login screen
                requireActivity().finish()
                // Start LoginActivity
                startActivity(android.content.Intent(requireContext(), com.example.airadvise.activities.LoginActivity::class.java))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingState.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}