package com.example.airadvise.utils

import com.example.airadvise.models.Activity
import com.example.airadvise.models.AirQualityForecast

/**
 * Calculate best day for specific activities based on air quality forecasts
 */
class BestDayRecommendation {
    companion object {
        /**
         * Get the best day for a specific activity from a list of forecasts
         * @param forecasts List of air quality forecasts
         * @param activity The activity to find the best day for
         * @return The best day forecast or null if no suitable day is found
         */

        fun getBestDayForActivity(
            forecasts: List<AirQualityForecast>,
            activity: Activity
        ): AirQualityForecast? {
            if (forecasts.isEmpty()) return null

            // Map of intensity levels to threshold values
            val thresholds = mapOf(
                "low" to 100,      // Low intensity activities (walking, light gardening)
                "moderate" to 75,  // Moderate intensity (hiking, cycling)
                "high" to 50       // High intensity (running, sports)
            )

            // Get the threshold for this activity - either from the predefined map if it's a standard level,
            // or use the directly provided threshold value
            val activityThreshold = if (activity.intensityLevel.lowercase() in thresholds) {
                thresholds[activity.intensityLevel.lowercase()] ?: 75
            } else {
                // If not a standard intensity level, use the value from the activity
                activity.thresholdValue
            }

            // Filter days that are below the threshold
            val suitableDays = forecasts.filter { it.aqi <= activityThreshold }

            // If no days meet the threshold, return the day with lowest AQI
            if (suitableDays.isEmpty()) {
                return forecasts.minByOrNull { it.aqi }
            }

            // If we have suitable days, pick the one with the best (lowest) AQI
            return suitableDays.minByOrNull { it.aqi }
        }

        /**
         * Check if a specific day is good for an activity
         * @param forecast The forecast for the day
         * @return true if the day is suitable for the activity
         */

        fun isDaySuitableForActivity(
            forecast: AirQualityForecast,
            activity: Activity
        ): Boolean {
            val thresholds = mapOf(
                "low" to 100,
                "moderate" to 75,
                "high" to 50
            )

            val activityThreshold = if (activity.intensityLevel.lowercase() in thresholds) {
                thresholds[activity.intensityLevel.lowercase()] ?: 75
            } else {
                activity.thresholdValue
            }
            
            return forecast.aqi <= activityThreshold
        }
    }
}