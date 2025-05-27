package com.example.airadvise.models

enum class PollutantType {
    AQI, NO2, PM25, PM10, O3
}

data class Pollutant(
    val code: String,
    val name: String,
    val value: Double,
    val unit: String,
    val index: Int,
    val description: String
)

enum class PollutantLevel(val color: Int) {
    GOOD(R.color.pollutant_good),
    MODERATE(R.color.pollutant_moderate),
    UNHEALTHY_SENSITIVE(R.color.pollutant_unhealthy_sensitive),
    UNHEALTHY(R.color.pollutant_unhealthy),
    VERY_UNHEALTHY(R.color.pollutant_very_unhealthy),
    HAZARDOUS(R.color.pollutant_hazardous)
}