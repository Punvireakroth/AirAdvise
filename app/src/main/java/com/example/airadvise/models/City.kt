package com.example.airadvise.models

data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    var isFavorite: Boolean = false,
    var lastSearched: Long? = null
)