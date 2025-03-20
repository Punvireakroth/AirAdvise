package com.example.airadvise.models.request

data class FeedbackRequest(
    val subject: String,
    val message: String
)

data class FeedbackResponseRequest(
    val response: String
)