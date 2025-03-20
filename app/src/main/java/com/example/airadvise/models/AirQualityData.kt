package com.example.airadvise.models

data class AirQualityData(
    val id: Long,
    val locationId: Long,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val o3: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?,
    val category: String,
    val source: String,
    val timestamp: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)