package com.example.airadvise.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CityDao {
    @Query("SELECT * FROM cities ORDER BY name ASC")
    fun getAllCities(): kotlin.collections.List<com.example.airadvise.database.CityEntity>
    
    @Query("SELECT * FROM cities WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCities(): List<CityEntity>
    
    @Query("SELECT * FROM cities ORDER BY lastSearched DESC LIMIT 5")
    fun getRecentSearches(): List<CityEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: CityEntity)
    
    @Update
    fun updateCity(city: CityEntity)
    
    @Delete
    fun deleteCity(city: CityEntity)
    
    @Query("UPDATE cities SET isFavorite = :isFavorite WHERE id = :cityId")
    fun updateFavoriteStatus(cityId: String, isFavorite: Boolean)
    
    @Query("UPDATE cities SET lastSearched = :timestamp WHERE id = :cityId")
    fun updateLastSearched(cityId: String, timestamp: Long)
    
    @Query("SELECT * FROM cities WHERE id = :cityId LIMIT 1")
    fun getCityById(cityId: String): CityEntity?
}