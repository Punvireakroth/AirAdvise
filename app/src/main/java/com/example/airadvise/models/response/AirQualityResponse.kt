package com.example.airadvise.models.response

import com.example.airadvise.models.Activity
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.HealthTip
import com.example.airadvise.models.Location
import com.example.airadvise.models.Pollutant
import com.google.gson.annotations.SerializedName

data class AirQualityResponseData(
    @SerializedName("air_quality") val airQualityData: AirQualityData,
    @SerializedName("safe_activities") val safeActivities: List<Activity>?,
    @SerializedName("unsafe_activities") val unsafeActivities: List<Activity>?,
    @SerializedName("health_tips") val healthTips: List<HealthTip>?
)

