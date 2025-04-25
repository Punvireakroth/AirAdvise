package com.example.airadvise.models

data class RecommendedActivities(
    val low: List<String>? = null,
    val moderate: List<String>? = null,
    val high: List<String>? = null
) 