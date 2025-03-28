package com.example.airadvise.models

data class Pollutant(
    val code: String,
    val name: String,
    val value: Double,
    val unit: String,
    val index: Int,
    val description: String
)