package com.example.airadvise.models

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id")
    val id: String,
    val name: String,
    val country: String,
    val region: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val population: Int? = null,
    var isFavorite: Boolean = false,
    var lastSearched: Long? = null
)