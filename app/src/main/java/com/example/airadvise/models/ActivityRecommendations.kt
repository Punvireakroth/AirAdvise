package com.example.airadvise.models

data class ActivityRecommendations(
    val safeActivities: List<Activity>,
    val unsafeActivities: List<Activity>
)