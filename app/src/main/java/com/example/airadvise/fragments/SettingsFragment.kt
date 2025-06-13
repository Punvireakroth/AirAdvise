package com.example.airadvise.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airadvise.R
import com.example.airadvise.activities.LoginActivity
import com.example.airadvise.adapters.FeedbackAdapter
import com.example.airadvise.api.ApiClient
import com.example.airadvise.models.Feedback
import com.example.airadvise.models.request.FeedbackRequest
import com.example.airadvise.utils.LocaleHelper
import com.example.airadvise.utils.Resource
import com.example.airadvise.utils.SessionManager
import com.example.airadvise.utils.safeApiCall
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Set up language preference
        findPreference<ListPreference>("language")?.setOnPreferenceChangeListener { _, newValue ->
            val language = newValue as String
            activity?.let {
                LocaleHelper.applyLanguageAndRecreate(it, language)
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
        
        // Set up submit feedback preference
        findPreference<Preference>("submit_feedback")?.setOnPreferenceClickListener {
            showSubmitFeedbackDialog()
            true
        }
        
        // Set up view feedback responses preference
        findPreference<Preference>("view_feedback_responses")?.setOnPreferenceClickListener {
            showFeedbackResponsesDialog()
            true
        }
    }

    private fun showSubmitFeedbackDialog() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_submit_feedback, null)
        val subjectEditText = view.findViewById<EditText>(R.id.editTextSubject)
        val messageEditText = view.findViewById<EditText>(R.id.editTextMessage)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Submit Feedback")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->
                val subject = subjectEditText.text.toString().trim()
                val message = messageEditText.text.toString().trim()
                
                if (subject.isEmpty() || message.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                submitFeedback(subject, message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun submitFeedback(subject: String, message: String) {
        lifecycleScope.launch {
            try {
                val loadingDialog = AlertDialog.Builder(requireContext())
                    .setMessage("Submitting feedback...")
                    .setCancelable(false)
                    .show()
                
                val request = FeedbackRequest(subject, message)
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).submitFeedback(request)
                }
                
                loadingDialog.dismiss()
                
                when (response) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // Already showing loading dialog
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showFeedbackResponsesDialog() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback_responses, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFeedback)
        val emptyView = view.findViewById<TextView>(R.id.textViewEmpty)
        val loadingView = view.findViewById<View>(R.id.loadingView)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Your Feedback History")
            .setView(view)
            .setPositiveButton("Close", null)
            .create()
            
        dialog.show()
        
        loadFeedbackResponses(recyclerView, emptyView, loadingView)
    }
    
    private fun loadFeedbackResponses(recyclerView: RecyclerView, emptyView: TextView, loadingView: View) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        
        loadingView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext()).getUserFeedback()
                }
                
                loadingView.visibility = View.GONE
                
                when (response) {
                    is Resource.Success -> {
                        val feedbackList = response.data?.get("feedback") ?: emptyList()
                        
                        if (feedbackList.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            
                            val adapter = FeedbackAdapter(requireContext(), feedbackList)
                            recyclerView.adapter = adapter
                        }
                    }
                    is Resource.Error -> {
                        emptyView.text = "Error: ${response.message}"
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                    is Resource.Loading -> {
                        // Already showing loading view
                    }
                }
            } catch (e: Exception) {
                loadingView.visibility = View.GONE
                emptyView.text = "Error: ${e.message}"
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
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