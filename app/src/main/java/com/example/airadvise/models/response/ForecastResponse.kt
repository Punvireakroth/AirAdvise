package com.example.airadvise.models.response

import com.example.airadvise.models.AirQualityForecast

data class ForecastResponse(
    val forecasts: List<AirQualityForecast>,
    val bestDay: AirQualityForecast?
)