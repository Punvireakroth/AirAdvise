package com.example.airadvise.models

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String = "user",
    val createdAt: String? = null,
    val updatedAt: String? = null
)