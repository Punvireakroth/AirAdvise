package com.example.airadvise.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.airadvise.activities.LoginActivity
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.ActivityMainBinding
import com.example.airadvise.utils.LocationPermissionManager
import com.example.airadvise.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import android.provider.Settings
import android.net.Uri
import com.example.airadvise.R
import com.example.airadvise.utils.LocaleHelper
import android.util.Log
import android.content.res.Configuration

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved locale to the context
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set theme from preferences
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val themeMode = prefs.getString("theme_mode", "-1")?.toInt() ?: -1
        AppCompatDelegate.setDefaultNightMode(themeMode)
        
        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        
        // Set up navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configure app bar to work with navigation
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.mapFragment, R.id.forecastFragment, R.id.settingsFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Handle deep links if any
        handleIntent(intent)

        setupLogout()
        checkLocationPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // Check if there's a city ID to load
        intent.getStringExtra(EXTRA_CITY_ID)?.let { cityId ->
            val bundle = Bundle().apply {
                putString("cityId", cityId)
            }
            navController.navigate(R.id.mapFragment, bundle)
        }
    }

    private fun setupLogout() {
        // Assuming you have a logout button or menu item
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Show loading
                binding.progressBar.visibility = View.VISIBLE
                
                // Call logout API
                val response = ApiClient.createApiService(this@MainActivity).logout()
                
                // Clear local session regardless of response
                SessionManager.clearSession(this@MainActivity)
                
                // Navigate to login screen
                navigateToLogin()
            } catch (e: Exception) {
                // Even if API call fails, clear local session and go to login
                SessionManager.clearSession(this@MainActivity)
                navigateToLogin()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkLocationPermissions() {
        val locationPermissionManager = LocationPermissionManager(this)
        locationPermissionManager.checkAndRequestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionManager = LocationPermissionManager(this)

        val handled = locationPermissionManager.handlePermissionResult(
            requestCode,
            permissions,
            grantResults
        )

        if (!handled) {
            // Handle the case where the user denied the permission
            showPermissionDeniedMessage()
        }
    }

    // Show a message to the user that the location permission is required
    private fun showPermissionDeniedMessage() {
        Snackbar.make(
            binding.root,
            getString(R.string.location_permission_required),
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            // Open app settings to enable location permission
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        }.show()
    }
    
    // Handle back button to prevent accidental app exit
    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.homeFragment) {
            navController.navigate(R.id.homeFragment)
        } else {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the saved locale when configuration changes
        val language = LocaleHelper.getPersistedLanguage(this)
        LocaleHelper.setLocale(this, language)
    }

    companion object {
        const val EXTRA_CITY_ID = "city_id"
    }
}
