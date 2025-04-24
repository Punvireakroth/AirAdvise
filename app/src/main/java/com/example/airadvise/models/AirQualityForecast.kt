package com.example.airadvise.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AirQualityForecast(
    val id: Long,
    val locationId: Long,
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
    val createdAt: String? = null,
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
        if (forecastDate.isNullOrEmpty()) return "Unknown Day"
        
        return try {
            val date = try {
                LocalDate.parse(forecastDate, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: DateTimeParseException) {
                try {
                    LocalDate.parse(forecastDate, dateFormatter)
                } catch (e: DateTimeParseException) {
                    try {
                        LocalDate.parse(forecastDate, fallbackFormatter)
                    } catch (e: DateTimeParseException) {
                        if (forecastDate.contains("T")) {
                            LocalDate.parse(forecastDate.split("T")[0], fallbackFormatter)
                        } else {
                            return "Unknown Day"
                        }
                    }
                }
            }
            
            date.format(DateTimeFormatter.ofPattern("EEEE"))
        } catch (e: Exception) {
            android.util.Log.e("AirQualityForecast", "Date parsing error: ${e.message} for date: $forecastDate")
            "Unknown Day"
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun getShortDate(): String {
        if (forecastDate.isNullOrEmpty()) return "?"
        
        return try {
            val date = try {
                LocalDate.parse(forecastDate, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: DateTimeParseException) {
                try {
                    LocalDate.parse(forecastDate, dateFormatter)
                } catch (e: DateTimeParseException) {
                    try {
                        LocalDate.parse(forecastDate, fallbackFormatter)
                    } catch (e: DateTimeParseException) {
                        if (forecastDate.contains("T")) {
                            LocalDate.parse(forecastDate.split("T")[0], fallbackFormatter)
                        } else {
                            return "?"
                        }
                    }
                }
            }
            date.format(DateTimeFormatter.ofPattern("EEE"))
        } catch (e: Exception) {
            android.util.Log.e("AirQualityForecast", "Short date parsing error: ${e.message} for date: $forecastDate")
            "?"
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