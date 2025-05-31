package com.example.airadvise.models

import com.google.gson.annotations.SerializedName

data class UserPreferences(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerializedName("daily_forecast_enabled") val dailyForecastEnabled: Boolean = true,
    @SerializedName("aqi_threshold") val aqiThreshold: Int = 100,
    @SerializedName("language") val language: String = "en",
    @SerializedName("dark_mode") val darkMode: Boolean? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)