package com.example.airadvise.models.request

data class UpdateUserRequest(
    val name: String,
    val email: String
)

data class ChangePasswordRequest(
    val current_password: String,
    val password: String,
    val password_confirmation: String
)