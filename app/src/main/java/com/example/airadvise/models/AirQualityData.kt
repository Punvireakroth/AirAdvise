package com.example.airadvise.models

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class AirQualityData(
    val id: Long,
    @SerializedName("location_id") val locationId: Long,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val o3: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?,
    val category: String,
    val source: String,
    val timestamp: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isLive: Boolean = true,
    
    @Transient
    @SerializedName("pollutants")
    private val rawPollutants: String? = null
) {
    val pollutants: Map<PollutantType, Pollutant>
        get() = try {
            getPollutants().associateBy { it.type }
        } catch (e: Exception) {
            emptyMap()
        }
    
    // Extract the pollutants values
    fun getPollutants(): List<Pollutant> {
        val pollutants = mutableListOf<Pollutant>()

        // Add PM2.5
        pollutants.add(
            Pollutant(
                type = PollutantType.PM25,
                value = pm25,
                unit = "μg/m³",
                level = getPM25Level(pm25),
                code = "pm25",
                name = "PM2.5",
                index = calculatePM25Index(pm25),
                description = "Fine particulate matter that can penetrate deep into the lungs and bloodstream."
            )
        )

        // Add PM10
        pollutants.add(
            Pollutant(
                type = PollutantType.PM10,
                value = pm10,
                unit = "μg/m³",
                level = getPM10Level(pm10),
                code = "pm10",
                name = "PM10",
                index = calculatePM10Index(pm10),
                description = "Inhalable particles, with diameters generally 10 micrometers and smaller."
            )
        )

        // Add Ozone (O3)
        o3?.let {
            pollutants.add(
                Pollutant(
                    type = PollutantType.O3,
                    value = it,
                    unit = "ppb",
                    level = getO3Level(it),
                    code = "o3",
                    name = "Ozone (O₃)",
                    index = calculateO3Index(it),
                    description = "A reactive gas that can affect the lungs at higher levels."
                )
            )
        }

        // Add Nitrogen Dioxide (NO2)
        no2?.let {
            pollutants.add(
                Pollutant(
                    type = PollutantType.NO2,
                    value = it,
                    unit = "ppb",
                    level = getNO2Level(it),
                    code = "no2",
                    name = "Nitrogen Dioxide (NO₂)",
                    index = calculateNO2Index(it),
                    description = "A gas that can irritate airways in the human respiratory system."
                )
            )
        }

        // Add Sulfur Dioxide (SO2)
        so2?.let {
            pollutants.add(
                Pollutant(
                    type = PollutantType.SO2,
                    value = it,
                    unit = "ppb",
                    level = getSO2Level(it),
                    code = "so2",
                    name = "Sulfur Dioxide (SO₂)",
                    index = calculateSO2Index(it),
                    description = "A gas that can harm the human respiratory system and make breathing difficult."
                )
            )
        }

        // Add Carbon Monoxide (CO)
        co?.let {
            pollutants.add(
                Pollutant(
                    type = PollutantType.CO,
                    value = it,
                    unit = "ppm",
                    level = getCOLevel(it),
                    code = "co",
                    name = "Carbon Monoxide (CO)",
                    index = calculateCOIndex(it),
                    description = "A gas that reduces oxygen delivery to the body's organs."
                )
            )
        }

        return pollutants
    }

    // Helper methods to determine pollution levels
    private fun getPM25Level(value: Double): PollutantLevel {
        return when {
            value <= 12.0 -> PollutantLevel.GOOD
            value <= 35.4 -> PollutantLevel.MODERATE
            value <= 55.4 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 150.4 -> PollutantLevel.UNHEALTHY
            value <= 250.4 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }

    private fun getPM10Level(value: Double): PollutantLevel {
        return when {
            value <= 54 -> PollutantLevel.GOOD
            value <= 154 -> PollutantLevel.MODERATE
            value <= 254 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 354 -> PollutantLevel.UNHEALTHY
            value <= 424 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }

    private fun getO3Level(value: Double): PollutantLevel {
        return when {
            value <= 30 -> PollutantLevel.GOOD
            value <= 60 -> PollutantLevel.MODERATE
            value <= 90 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 120 -> PollutantLevel.UNHEALTHY
            value <= 150 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }

    private fun getNO2Level(value: Double): PollutantLevel {
        return when {
            value <= 20 -> PollutantLevel.GOOD
            value <= 40 -> PollutantLevel.MODERATE
            value <= 80 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 120 -> PollutantLevel.UNHEALTHY
            value <= 180 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }

    private fun getSO2Level(value: Double): PollutantLevel {
        return when {
            value <= 35 -> PollutantLevel.GOOD
            value <= 75 -> PollutantLevel.MODERATE
            value <= 185 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 305 -> PollutantLevel.UNHEALTHY
            value <= 605 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }

    private fun getCOLevel(value: Double): PollutantLevel {
        return when {
            value <= 1 -> PollutantLevel.GOOD
            value <= 2 -> PollutantLevel.MODERATE
            value <= 10 -> PollutantLevel.UNHEALTHY_SENSITIVE
            value <= 20 -> PollutantLevel.UNHEALTHY
            value <= 50 -> PollutantLevel.VERY_UNHEALTHY
            else -> PollutantLevel.HAZARDOUS
        }
    }

    // Simplified calculations.
    // TODO: use more accurate EPA formulas.
    private fun calculatePM25Index(value: Double): Int = (value * 2).toInt().coerceIn(0, 100)
    private fun calculatePM10Index(value: Double): Int = (value / 2.5).toInt().coerceIn(0, 100)
    private fun calculateO3Index(value: Double): Int = (value / 1.5).toInt().coerceIn(0, 100)
    private fun calculateNO2Index(value: Double): Int = (value / 2).toInt().coerceIn(0, 100)
    private fun calculateSO2Index(value: Double): Int = (value * 3).toInt().coerceIn(0, 100)
    private fun calculateCOIndex(value: Double): Int = (value * 30).toInt().coerceIn(0, 100)
}