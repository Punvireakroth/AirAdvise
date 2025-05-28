package com.example.airadvise.models

import com.example.airadvise.R

enum class PollutantType {
    AQI, NO2, PM25, PM10, O3, SO2, CO
}

enum class PollutantLevel(val color: Int) {
    GOOD(R.color.pollutant_good),
    MODERATE(R.color.pollutant_moderate),
    UNHEALTHY_SENSITIVE(R.color.pollutant_unhealthy_sensitive),
    UNHEALTHY(R.color.pollutant_unhealthy),
    VERY_UNHEALTHY(R.color.pollutant_very_unhealthy),
    HAZARDOUS(R.color.pollutant_hazardous)
}

data class Pollutant(
    val type: PollutantType,
    val value: Double,
    val unit: String,
    val level: PollutantLevel,
    val code: String? = null,
    val name: String? = null,
    val index: Int? = null,
    val description: String? = null
)