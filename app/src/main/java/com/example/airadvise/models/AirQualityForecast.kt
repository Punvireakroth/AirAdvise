package com.example.airadvise.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AirQualityForecast(
    val id: Long,
    val locationId: Long,
    val forecastDate: String,
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
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    fun getFormattedDate(): String {
        val date = LocalDate.parse(forecastDate, dateFormatter)
        return date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }

    fun getShortDate(): String {
        val date = LocalDate.parse(forecastDate, dateFormatter)
        return date.format(DateTimeFormatter.ofPattern("EEE"))
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