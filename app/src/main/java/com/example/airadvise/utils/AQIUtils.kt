package com.example.airadvise.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.airadvise.R

object AQIUtils {
    // AQI Category Ranges
    const val AQI_GOOD_MAX = 50
    const val AQI_MODERATE_MAX = 100
    const val AQI_UNHEALTHY_SENSITIVE_MAX = 150
    const val AQI_UNHEALTHY_MAX = 200
    const val AQI_VERY_UNHEALTHY_MAX = 300

    // Get category label based on AQI value
    fun getAQICategory(context: Context, aqiValue: Int): String {
        return when {
            aqiValue <= AQI_GOOD_MAX -> context.getString(R.string.aqi_good)
            aqiValue <= AQI_MODERATE_MAX -> context.getString(R.string.aqi_moderate)
            aqiValue <= AQI_UNHEALTHY_SENSITIVE_MAX -> context.getString(R.string.aqi_unhealthy_sensitive)
            aqiValue <= AQI_UNHEALTHY_MAX -> context.getString(R.string.aqi_unhealthy)
            aqiValue <= AQI_VERY_UNHEALTHY_MAX -> context.getString(R.string.aqi_very_unhealthy)
            else -> context.getString(R.string.aqi_hazardous)
        }
    }

    // Get color resource ID for AQI value
    fun getAQIColorResource(aqiValue: Int): Int {
        return when {
            aqiValue <= AQI_GOOD_MAX -> R.color.aqi_good
            aqiValue <= AQI_MODERATE_MAX -> R.color.aqi_moderate
            aqiValue <= AQI_UNHEALTHY_SENSITIVE_MAX -> R.color.aqi_unhealthy_sensitive
            aqiValue <= AQI_UNHEALTHY_MAX -> R.color.aqi_unhealthy
            aqiValue <= AQI_VERY_UNHEALTHY_MAX -> R.color.aqi_very_unhealthy
            else -> R.color.aqi_hazardous
        }
    }

    // Get actual color for AQI value
    fun getAQIColor(context: Context, aqiValue: Int): Int {
        return ContextCompat.getColor(context, getAQIColorResource(aqiValue))
    }

    // Get health implications for AQI category
    fun getHealthImplications(context: Context, aqiValue: Int): String {
        return when {
            aqiValue <= AQI_GOOD_MAX -> context.getString(R.string.aqi_good_implications)
            aqiValue <= AQI_MODERATE_MAX -> context.getString(R.string.aqi_moderate_implications)
            aqiValue <= AQI_UNHEALTHY_SENSITIVE_MAX -> context.getString(R.string.aqi_unhealthy_sensitive_implications)
            aqiValue <= AQI_UNHEALTHY_MAX -> context.getString(R.string.aqi_unhealthy_implications)
            aqiValue <= AQI_VERY_UNHEALTHY_MAX -> context.getString(R.string.aqi_very_unhealthy_implications)
            else -> context.getString(R.string.aqi_hazardous_implications)
        }
    }

    // Get precautions for AQI category
    fun getPrecautions(context: Context, aqiValue: Int): String {
        return when {
            aqiValue <= AQI_GOOD_MAX -> context.getString(R.string.aqi_good_precautions)
            aqiValue <= AQI_MODERATE_MAX -> context.getString(R.string.aqi_moderate_precautions)
            aqiValue <= AQI_UNHEALTHY_SENSITIVE_MAX -> context.getString(R.string.aqi_unhealthy_sensitive_precautions)
            aqiValue <= AQI_UNHEALTHY_MAX -> context.getString(R.string.aqi_unhealthy_precautions)
            aqiValue <= AQI_VERY_UNHEALTHY_MAX -> context.getString(R.string.aqi_very_unhealthy_precautions)
            else -> context.getString(R.string.aqi_hazardous_precautions)
        }
    }
}