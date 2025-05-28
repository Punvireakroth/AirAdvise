package com.example.airadvise.utils

import com.example.airadvise.R
import com.example.airadvise.models.PollutantLevel
import com.example.airadvise.models.PollutantType

object PollutantColorUtils {
    fun getColorForAQI(aqi: Int): Int {
        return when {
            aqi <= 50 -> R.color.pollutant_good
            aqi <= 100 -> R.color.pollutant_moderate
            aqi <= 150 -> R.color.pollutant_unhealthy_sensitive
            aqi <= 200 -> R.color.pollutant_unhealthy
            aqi <= 300 -> R.color.pollutant_very_unhealthy
            else -> R.color.pollutant_hazardous
        }
    }
    
    fun getLevelForPollutant(type: PollutantType, value: Float): PollutantLevel {
        return when (type) {
            PollutantType.AQI -> getLevelForAQI(value.toInt())
            PollutantType.NO2 -> getLevelForNO2(value)
            PollutantType.PM25 -> getLevelForPM25(value)
            PollutantType.PM10 -> getLevelForPM10(value)
            PollutantType.O3 -> getLevelForO3(value)
            PollutantType.SO2 -> getLevelForSO2(value)
            PollutantType.CO -> getLevelForCO(value)
        }
    }
    
    fun getLevelForAQI(aqi: Int): PollutantLevel {
        return when {
            aqi <= 50 -> PollutantLevel.GOOD
            aqi <= 100 -> PollutantLevel.MODERATE
            aqi <= 150 -> PollutantLevel.UNHEALTHY_SENSITIVE
            aqi <= 200 -> PollutantLevel.UNHEALTHY
            aqi <= 300 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
    
    fun getLevelForNO2(value: Float): PollutantLevel {
        return when {
            value <= 53 -> PollutantLevel.GOOD
            value <= 100 -> PollutantLevel.MODERATE
            value <= 360 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 649 -> PollutantLevel.UNHEALTHY
            value <= 1249 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
    
    fun getLevelForPM25(value: Float): PollutantLevel {
        return when {
            value <= 12 -> PollutantLevel.GOOD
            value <= 35.4 -> PollutantLevel.MODERATE
            value <= 55.4 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 150.4 -> PollutantLevel.UNHEALTHY
            value <= 250.4 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
    
    fun getLevelForPM10(value: Float): PollutantLevel {
        return when {
            value <= 54 -> PollutantLevel.GOOD
            value <= 154 -> PollutantLevel.MODERATE
            value <= 254 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 354 -> PollutantLevel.UNHEALTHY
            value <= 424 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
    
    fun getLevelForO3(value: Float): PollutantLevel {
        return when {
            value <= 54 -> PollutantLevel.GOOD
            value <= 70 -> PollutantLevel.MODERATE
            value <= 85 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 105 -> PollutantLevel.UNHEALTHY
            value <= 200 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
    
    fun getLevelForSO2(value: Float): PollutantLevel {
        return when {
            value <= 35 -> PollutantLevel.GOOD
            value <= 75 -> PollutantLevel.MODERATE
            value <= 185 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 304 -> PollutantLevel.UNHEALTHY
            value <= 604 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
    
    fun getLevelForCO(value: Float): PollutantLevel {
        return when {
            value <= 4.4 -> PollutantLevel.GOOD
            value <= 9.4 -> PollutantLevel.MODERATE
            value <= 12.4 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 15.4 -> PollutantLevel.UNHEALTHY
            value <= 30.4 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }
}