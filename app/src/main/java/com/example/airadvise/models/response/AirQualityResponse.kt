package com.example.airadvise.models.response

import com.example.airadvise.models.Activity
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.HealthTip
import com.example.airadvise.models.Location
import com.example.airadvise.models.Pollutant
import com.google.gson.annotations.SerializedName

data class AirQualityResponseData(
    @SerializedName("air_quality") val airQualityData: AirQualityData,
    @SerializedName("safe_activities") val safeActivities: List<Activity>?,
    @SerializedName("unsafe_activities") val unsafeActivities: List<Activity>?,
    @SerializedName("health_tips") val healthTips: List<HealthTip>?
)

// data class AirQualityResponse(
//     @SerializedName("aqi") val aqi: Int,
//     @SerializedName("location") val location: Location,
//     @SerializedName("pollutants") val pollutants: List<Pollutant>,
//     @SerializedName("timestamp") val timestamp: String
// ) {
//     fun toAirQualityData(): AirQualityData {
//         // Extract pollutant values from the pollutants list
//         val pm25 = pollutants.find { it.code == "pm25" }?.value ?: 0.0
//         val pm10 = pollutants.find { it.code == "pm10" }?.value ?: 0.0
//         val o3 = pollutants.find { it.code == "o3" }?.value
//         val no2 = pollutants.find { it.code == "no2" }?.value
//         val so2 = pollutants.find { it.code == "so2" }?.value
//         val co = pollutants.find { it.code == "co" }?.value
        
//         // Generate a category based on AQI value
//         val category = when {
//             aqi <= 50 -> "Good"
//             aqi <= 100 -> "Moderate"
//             aqi <= 150 -> "Unhealthy for Sensitive Groups"
//             aqi <= 200 -> "Unhealthy"
//             aqi <= 300 -> "Very Unhealthy"
//             else -> "Hazardous"
//         }
        
//         return AirQualityData(
//             id = 0, // Use a temporary ID
//             locationId = location.id ?: 0, // Use location ID or default to 0
//             aqi = aqi,
//             pm25 = pm25,
//             pm10 = pm10,
//             o3 = o3,
//             no2 = no2,
//             so2 = so2,
//             co = co,
//             category = category,
//             source = "AirAdvise API", // Default source or extract from response if available
//             timestamp = timestamp,
//             createdAt = null,
//             updatedAt = null
//         )
//     }
// }
