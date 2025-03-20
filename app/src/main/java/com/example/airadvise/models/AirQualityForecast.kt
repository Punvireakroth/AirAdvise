package com.example.airadvise.models
data class AirQualityForecast(
    val id: Long,
    val locationId: Long,
    val forecastDate: String,
    val aqi: Int,
    val pm25: Double?,
    val pm10: Double?,
    val category: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)