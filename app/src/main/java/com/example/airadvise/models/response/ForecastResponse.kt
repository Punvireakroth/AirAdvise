package com.example.airadvise.models.response

import com.example.airadvise.models.AirQualityForecast
import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("forecasts") val forecasts: List<AirQualityForecast> = emptyList(),
    @SerializedName("best_day") val bestDay: AirQualityForecast? = null
)