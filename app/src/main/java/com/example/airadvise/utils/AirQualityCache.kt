package com.example.airadvise.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.airadvise.models.AirQualityData
import com.google.gson.Gson
import java.lang.Exception

object AirQualityCache {
    private const val PREF_NAME = "air_quality_cache"
    private const val KEY_DATA = "cache_air_quality_data"
    private const val KEY_TIMESTAMP = "cached_timestamp"

    // Cache expiration 30mn
    private const val CACHE_VALIDITY_MS = 30 * 60 * 1000

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAirQualityData(context: Context, airQualityData: AirQualityData) {
        val gson = Gson()
        val dataJson = gson.toJson(airQualityData)

        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_DATA, dataJson)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getCachedAirQualityData(context: Context): AirQualityData? {
        val prefs = getPrefs(context)

        val dataJson = prefs.getString(KEY_DATA, null) ?: return null
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)

        // Check if cache is still valid
        if (System.currentTimeMillis() - timestamp > CACHE_VALIDITY_MS) {
            return null
        }

        val gson = Gson()
        try {
            return gson.fromJson(dataJson, AirQualityData::class.java)
        } catch(e: Exception) {
            return null
        }
    }

    fun clearCache(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    // Check if cache is very fresh (< 10 minutes)
    fun isCacheVeryFresh(context: Context): Boolean {
        val prefs = getPrefs(context)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)
        
        return System.currentTimeMillis() - timestamp < 10 * 60 * 1000
    }

    fun saveAirQualityDataWithLocation(requireContext: Context, airQualityData: AirQualityData) {

    }
}