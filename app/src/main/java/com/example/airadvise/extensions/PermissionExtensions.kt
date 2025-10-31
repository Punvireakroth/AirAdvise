package com.example.airadvise.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Extension functions for location permission handling
 */

/**
 * Check if the app has location permission (either FINE or COARSE)
 */
fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Check if the fragment's context has location permission
 */
fun Fragment.hasLocationPermission(): Boolean {
    return requireContext().hasLocationPermission()
}
