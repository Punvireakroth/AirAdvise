package com.example.airadvise.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Feedback(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    val subject: String,
    val message: String,
    val status: String, 
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val responses: List<FeedbackResponse> = emptyList()
)

