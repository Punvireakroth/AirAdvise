package com.example.airadvise.models

data class UserPreferences(
    val id: Long,
    val userId: Long,
    val notificationEnabled: Boolean = true,
    val aqiThreshold: Int = 100,
    val preferredLanguage: String = "en",
    val temperatureUnit: String = "celsius",
    val createdAt: String? = null,
    val updatedAt: String? = null
)