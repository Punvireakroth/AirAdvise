package com.example.airadvise.models.response

data class MapAirQualityResponse(
    val mapUrl: String,
    val attribution: String,
    val timestamp: String, 
    val data: AirQualityMapData? = null
)

data class AirQualityMapData(
    val timestamp: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val o3: Double,
    val no2: Double,
    val so2: Double,
    val co: Double
)