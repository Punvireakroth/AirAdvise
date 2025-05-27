package com.example.airadvise.models.response

data class MapAirQualityResponse(
    val mapUrl: String,
    val attribution: String,
    val timestamp: Long
)