package com.example.airadvise.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.airadvise.models.PollutantType

class PreferenceManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun saveLastViewedCityId(cityId: String) {
        prefs.edit().putString(KEY_LAST_CITY_ID, cityId).apply()
    }
    
    fun getLastViewedCityId(): String? {
        return prefs.getString(KEY_LAST_CITY_ID, null)
    }
    
    fun saveSelectedPollutant(pollutant: PollutantType) {
        prefs.edit().putString(KEY_SELECTED_POLLUTANT, pollutant.name).apply()
    }
    
    fun getSelectedPollutant(): PollutantType {
        val pollutantName = prefs.getString(KEY_SELECTED_POLLUTANT, PollutantType.AQI.name)
        return try {
            PollutantType.valueOf(pollutantName ?: PollutantType.AQI.name)
        } catch (e: Exception) {
            PollutantType.AQI
        }
    }
    
    companion object {
        private const val PREFS_NAME = "air_advise_prefs"
        private const val KEY_LAST_CITY_ID = "last_city_id"
        private const val KEY_SELECTED_POLLUTANT = "selected_pollutant"
        
        @Volatile
        private var INSTANCE: PreferenceManager? = null
        
        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}