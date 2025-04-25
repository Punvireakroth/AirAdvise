package com.example.airadvise.models

data class Activity(
    val id: Long,
    val name: String,
    val intensityLevel: String,
    val thresholdValue: Int
)