package com.example.airadvise.models
data class Activity(
    val id: Long,
    val name: String,
    val description: String?,
    val maxSafeAqi: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)