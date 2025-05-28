package com.example.airadvise.models

data class AirQualityDataMap(
    val cityId: String,
    val timestamp: Long,
    val aqi: Int,
    val pollutants: Map<PollutantType, Pollutant>,
    val isLive: Boolean = true
)