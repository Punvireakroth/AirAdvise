package com.example.airadvise.models

data class AirQualityData(
    val id: Long,
    val locationId: Long,
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
) {
    // Extract the pollutants values
    fun getPollutants(): List<Pollutant> {
        val pollutants = mutableListOf<Pollutant>()

        // Add PM2.5
        pollutants.add(
            Pollutant(
                code = "pm25",
                name = "PM2.5",
                value = pm25,
                unit = "μg/m³",
                index = calculatePM25Index(pm25),
                description = "Fine particulate matter that can penetrate deep into the lungs and bloodstream."
            )
        )

        // Add PM10
        pollutants.add(
            Pollutant(
                code = "pm10",
                name = "PM10",
                value = pm10,
                unit = "μg/m³",
                index = calculatePM10Index(pm10),
                description = "Inhalable particles, with diameters generally 10 micrometers and smaller."
            )
        )

        // Add Ozone (O3)
        o3?.let {
            pollutants.add(
                Pollutant(
                    code = "o3",
                    name = "Ozone (O₃)",
                    value = it,
                    unit = "ppb",
                    index = calculateO3Index(it),
                    description = "A reactive gas that can affect the lungs at higher levels."
                )
            )
        }

        // Add Nitrogen Dioxide (NO2)
        no2?.let {
            pollutants.add(
                Pollutant(
                    code = "no2",
                    name = "Nitrogen Dioxide (NO₂)",
                    value = it,
                    unit = "ppb",
                    index = calculateNO2Index(it),
                    description = "A gas that can irritate airways in the human respiratory system."
                )
            )
        }

        // Add Sulfur Dioxide (SO2)
        so2?.let {
            pollutants.add(
                Pollutant(
                    code = "so2",
                    name = "Sulfur Dioxide (SO₂)",
                    value = it,
                    unit = "ppb",
                    index = calculateSO2Index(it),
                    description = "A gas that can harm the human respiratory system and make breathing difficult."
                )
            )
        }

        // Add Carbon Monoxide (CO)
        co?.let {
            pollutants.add(
                Pollutant(
                    code = "co",
                    name = "Carbon Monoxide (CO)",
                    value = it,
                    unit = "ppm",
                    index = calculateCOIndex(it),
                    description = "A gas that reduces oxygen delivery to the body's organs."
                )
            )
        }

        return pollutants
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