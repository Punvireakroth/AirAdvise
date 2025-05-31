package com.example.airadvise.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PreferenceManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Last viewed city
    suspend fun saveLastViewedCityId(cityId: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_LAST_CITY_ID, cityId).apply()
    }

    suspend fun getLastViewedCityId(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_LAST_CITY_ID, null)
    }

    // User preferences
    fun saveNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun saveDailyForecastEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_FORECAST_ENABLED, enabled).apply()
    }

    fun getDailyForecastEnabled(): Boolean {
        return prefs.getBoolean(KEY_DAILY_FORECAST_ENABLED, true)
    }

    fun saveAqiThreshold(threshold: Int) {
        prefs.edit().putInt(KEY_AQI_THRESHOLD, threshold).apply()
    }

    fun getAqiThreshold(): Int {
        return prefs.getInt(KEY_AQI_THRESHOLD, 100)
    }

    fun saveLanguagePreference(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun getLanguagePreference(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun saveThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        // Apply theme immediately
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    companion object {
        private const val PREFS_NAME = "AirAdvisePrefs"
        private const val KEY_LAST_CITY_ID = "last_city_id"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_FORECAST_ENABLED = "daily_forecast_enabled"
        private const val KEY_AQI_THRESHOLD = "aqi_threshold"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME_MODE = "theme_mode"

        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also { instance = it }
            }
        }
    }
}