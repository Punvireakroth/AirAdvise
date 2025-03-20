package com.example.airadvise.models.response
data class MessageResponse(val message: String)

data class ErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>? = null
)