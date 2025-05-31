package com.example.airadvise.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.airadvise.R
import com.example.airadvise.activities.LoginActivity
import com.example.airadvise.api.ApiClient
import com.example.airadvise.utils.LocaleHelper
import com.example.airadvise.utils.SessionManager
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Set up language preference
        findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, newValue ->
            val language = newValue as String
            activity?.let {
                LocaleHelper.setLocale(it, language)
                
                // Recreate the activity to apply the language change
                it.recreate()
            }
            true
        }

        // Set up theme preference
        findPreference<ListPreference>("theme_mode")?.setOnPreferenceChangeListener { _, newValue ->
            val themeMode = (newValue as String).toInt()
            AppCompatDelegate.setDefaultNightMode(themeMode)
            true
        }

        // Set up change password preference
        findPreference<Preference>("change_password")?.setOnPreferenceClickListener {
            showChangePasswordDialog()
            true
        }

        // Set up logout preference
        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            showLogoutConfirmationDialog()
            true
        }
    }

    private fun showChangePasswordDialog() {
        // Create an EditText for the current password
        val currentPasswordEditText = android.widget.EditText(requireContext())
        currentPasswordEditText.hint = "Current Password"
        currentPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Create an EditText for the new password
        val newPasswordEditText = android.widget.EditText(requireContext())
        newPasswordEditText.hint = "New Password"
        newPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Create a layout to hold both EditTexts
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.addView(currentPasswordEditText)
        layout.addView(newPasswordEditText)
        layout.setPadding(50, 20, 50, 20)

        // Build the AlertDialog
        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                // Get the passwords
                val currentPassword = currentPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()

                // Validate inputs
                if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                    android.widget.Toast.makeText(requireContext(), "Please fill in all fields", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Call logout API
                val response = ApiClient.createApiService(requireContext()).logout()
                
                // Clear local session regardless of response
                SessionManager.clearSession(requireContext())
                
                // Navigate to login screen
                navigateToLogin()
            } catch (e: Exception) {
                // Even if API call fails, clear local session and go to login
                SessionManager.clearSession(requireContext())
                navigateToLogin()
            }
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
} 