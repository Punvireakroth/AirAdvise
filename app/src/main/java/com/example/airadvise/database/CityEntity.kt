package com.example.airadvise.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.airadvise.models.City

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false,
    val lastSearched: Long? = null
)

// Extension functions to convert between Entity and Model
fun CityEntity.toCity(): City = City(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude,
    isFavorite = isFavorite,
    lastSearched = lastSearched
)

fun City.toEntity(): CityEntity = CityEntity(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude,
    isFavorite = isFavorite,
    lastSearched = lastSearched
) 