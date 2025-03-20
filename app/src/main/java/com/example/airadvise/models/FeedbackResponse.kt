package com.example.airadvise.models
data class FeedbackResponse(
    val id: Long,
    val feedbackId: Long,
    val adminId: Long,
    val response: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)