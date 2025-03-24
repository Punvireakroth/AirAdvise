package com.example.airadvise

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.airadvise.activities.LoginActivity
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.ActivityMainBinding
import com.example.airadvise.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupLogout()
    }

    private fun setupNavigation() {
        // Set up Navigation Controller with Bottom Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Find the bottom navigation view
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)
        
        // Handle navigation destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // You can handle UI changes when destinations change
            when (destination.id) {
                R.id.homeFragment -> {
                    // Handle home fragment selected
                }
                R.id.forecastFragment -> {
                    // Handle forecast fragment selected
                }
                R.id.locationsFragment -> {
                    // Handle locations fragment selected
                }
                R.id.profileFragment -> {
                    // Handle profile fragment selected
                }
                // Add other destinations in the future
            }
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
    
    // Handle back button to prevent accidental app exit
    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.homeFragment) {
            navController.navigate(R.id.homeFragment)
        } else {
            super.onBackPressed()
        }
    }
}
