package com.example.airadvise

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.airadvise.utils.LocaleHelper

class AirAdviseApplication : Application() {
    override fun attachBaseContext(base: Context) {
        // Apply the saved locale to the context
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the saved locale when configuration changes
        val language = LocaleHelper.getPersistedLanguage(this)
        LocaleHelper.setLocale(this, language)
    }

    override fun onCreate() {
        super.onCreate()
        
        // Add for crash reporting
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AirAdviseApp", "Uncaught exception in thread ${thread.name}", throwable)
        }
        
        // Set theme from preferences
        try {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
            val themeMode = prefs.getString("theme_mode", "-1")?.toInt() ?: -1
            AppCompatDelegate.setDefaultNightMode(themeMode)
        } catch (e: Exception) {
            Log.e("AirAdviseApplication", "Error applying theme", e)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}