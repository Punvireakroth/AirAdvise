package com.example.airadvise.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AirQualityForecast(
    val id: Long,
    @SerializedName("location_id")
    val locationId: Long,
    @SerializedName("forecast_date")
    val forecastDate: String?,
    val aqi: Int,
    val pm25: Double?,
    val pm10: Double?,
    val o3: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?,
    val category: String,
    val description: String,
    val recommendation: String?,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        @RequiresApi(Build.VERSION_CODES.O)
        private val fallbackFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDate(): String {
        android.util.Log.d("AirQualityForecast", "getFormattedDate called with date: $forecastDate")
        
        // Check for null or empty
        if (forecastDate == null || forecastDate.isEmpty()) {
            android.util.Log.d("AirQualityForecast", "Date is null or empty")
            return "Unknown Day"
        }
        
        try {
            // Extract date part (before T)
            if (!forecastDate.contains("T")) {
                android.util.Log.d("AirQualityForecast", "No T in date: $forecastDate")
                return "Unknown Day"
            }
            
            val datePart = forecastDate.split("T")[0]
            android.util.Log.d("AirQualityForecast", "Extracted date part: $datePart")
            
            // Parse using standard ISO date format
            val date = LocalDate.parse(datePart)
            android.util.Log.d("AirQualityForecast", "Successfully parsed date: $date")
            
            // Format and return
            val formatted = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            android.util.Log.d("AirQualityForecast", "Formatted date: $formatted")
            return formatted
        } catch (e: Exception) {
            android.util.Log.e("AirQualityForecast", "Error formatting date", e)
            return "Unknown Day"
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun getShortDate(): String {
        android.util.Log.d("AirQualityForecast", "getShortDate called with date: $forecastDate")
        
        // Check for null or empty
        if (forecastDate == null || forecastDate.isEmpty()) {
            android.util.Log.d("AirQualityForecast", "Date is null or empty")
            return "?"
        }
        
        try {
            // Extract date part (before T)
            if (!forecastDate.contains("T")) {
                android.util.Log.d("AirQualityForecast", "No T in date: $forecastDate")
                return "?"
            }
            
            val datePart = forecastDate.split("T")[0]
            android.util.Log.d("AirQualityForecast", "Extracted date part: $datePart")
            
            // Parse using standard ISO date format
            val date = LocalDate.parse(datePart)
            android.util.Log.d("AirQualityForecast", "Successfully parsed date: $date")
            
            // Format and return
            val formatted = date.format(DateTimeFormatter.ofPattern("EEE"))
            android.util.Log.d("AirQualityForecast", "Formatted date: $formatted")
            return formatted
        } catch (e: Exception) {
            android.util.Log.e("AirQualityForecast", "Error formatting short date", e)
            return "?"
        }
    }
    
    fun getCategoryColor(): Int {
        return when (category.lowercase()) {
            "good" -> android.graphics.Color.parseColor("#A8E05F")
            "moderate" -> android.graphics.Color.parseColor("#FDD74B")
            "unhealthy for sensitive groups" -> android.graphics.Color.parseColor("#FB9B57")
            "unhealthy" -> android.graphics.Color.parseColor("#F76C5E")
            "very unhealthy" -> android.graphics.Color.parseColor("#A97ABC")
            "hazardous" -> android.graphics.Color.parseColor("#A87383")
            else -> android.graphics.Color.GRAY
        }
    }
}