package com.example.airadvise.utils

import android.view.View
import com.example.airadvise.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class LoadingStateManager(private val binding: ActivityMainBinding) {
    fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    fun showError(message: String, actionText: String? = null, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
        }

        snackbar.show()
    }
}