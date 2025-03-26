package com.example.airadvise.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationPermissionManager(private val activity: AppCompatActivity) {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 10001
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun checkAndRequestPermissions() {
        if (hasPermissions()) {
            // Permissions already granted, proceed with location request
        } else {
            requestPermissions()
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity, 
            requiredPermissions, 
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun handlePermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        return when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
               val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
               if (allGranted) {
                // Permissions granted, proceed with location request
                true
               } else {
                // Permissions denied, show explanation
                false
               }
            }
            else -> false
        }
    }    
}