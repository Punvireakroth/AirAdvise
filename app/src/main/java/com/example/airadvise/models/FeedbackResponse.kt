package com.example.airadvise.models

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(
    val id: Long,
    @SerializedName("feedback_id") val feedbackId: Long,
    @SerializedName("admin_id") val adminId: Long?,
    @SerializedName("admin_name") val adminName: String?,
    val message: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)