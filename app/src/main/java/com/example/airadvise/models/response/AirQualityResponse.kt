package com.example.airadvise.models.response

import com.example.airadvise.models.Activity
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.HealthTip
import com.example.airadvise.models.Location

data class AirQualityResponse(
    val airQuality: AirQualityData,
    val location: Location,
    val healthTips: List<HealthTip>,
    val safeActivities: List<Activity>,
    val unsafeActivities: List<Activity>
)