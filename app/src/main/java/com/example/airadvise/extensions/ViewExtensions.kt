package com.example.airadvise.extensions

import android.view.View
import android.widget.ProgressBar

/**
 * Extension functions for managing UI state (loading, content, error)
 */

/**
 * Show loading state by making the view visible
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hide view by making it gone
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Show progress bar
 */
fun ProgressBar.showLoading() {
    visibility = View.VISIBLE
}

/**
 * Hide progress bar
 */
fun ProgressBar.hideLoading() {
    visibility = View.GONE
}

/**
 * Set loading state for a progress bar
 */
fun ProgressBar.setLoading(isLoading: Boolean) {
    visibility = if (isLoading) View.VISIBLE else View.GONE
}
