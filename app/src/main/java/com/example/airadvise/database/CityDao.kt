package com.example.airadvise.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.airadvise.models.City

@Dao
interface CityDao {
    @Query("SELECT * FROM cities ORDER BY name ASC")
    suspend fun getAllCities(): List<CityEntity>
    
    @Query("SELECT * FROM cities WHERE isFavorite = 1 ORDER BY name ASC")
    suspend fun getFavoriteCities(): List<CityEntity>
    
    @Query("SELECT * FROM cities ORDER BY lastSearched DESC LIMIT 5")
    suspend fun getRecentSearches(): List<CityEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)
    
    @Update
    suspend fun updateCity(city: CityEntity)
    
    @Delete
    suspend fun deleteCity(city: CityEntity)
    
    @Query("UPDATE cities SET isFavorite = :isFavorite WHERE id = :cityId")
    suspend fun updateFavoriteStatus(cityId: String, isFavorite: Boolean)
    
    @Query("UPDATE cities SET lastSearched = :timestamp WHERE id = :cityId")
    suspend fun updateLastSearched(cityId: String, timestamp: Long)
    
    @Query("SELECT * FROM cities WHERE id = :cityId LIMIT 1")
    suspend fun getCityById(cityId: String): CityEntity?
}

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