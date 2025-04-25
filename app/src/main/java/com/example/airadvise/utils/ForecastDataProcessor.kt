package com.example.airadvise.utils

import com.example.airadvise.models.AirQualityForecast
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

/**
 * Utility class for processing forecast data for the chart
 */
class ForecastDataProcessor {
    companion object {
        /**
         * Process forecast data for a specific pollutant
         * @param forecasts List of forecasts
         * @param pollutant Pollutant type to extract (AQI, PM2.5, etc.)
         * @return Pair of entries for chart and x-axis labels
         */
        fun processDataForChart(
            forecasts: List<AirQualityForecast>,
            pollutant: String
        ): Pair<List<Entry>, List<String>> {
            val entries = mutableListOf<Entry>()
            val labels = mutableListOf<String>()

            forecasts.forEachIndexed { index, forecast ->
                val value = when (pollutant) {
                    "AQI" -> forecast.aqi.toFloat()
                    "PM2.5" -> forecast.pm25?.toFloat() ?: 0f
                    "PM10" -> forecast.pm10?.toFloat() ?: 0f
                    "O₃" -> forecast.o3?.toFloat() ?: 0f
                    "NO₂" -> forecast.no2?.toFloat() ?: 0f
                    "SO₂" -> forecast.so2?.toFloat() ?: 0f
                    "CO" -> forecast.co?.toFloat() ?: 0f
                    else -> forecast.aqi.toFloat()
                }

                entries.add(Entry(index.toFloat(), value))
                labels.add(forecast.getShortDate())
            }

            return Pair(entries, labels)
        }

        /**
         * Configure a line data set with appropriate styling
         * @param entries Data entries
         * @param label Label for the data set
         * @param color Line color
         * @return Configured LineDataSet
         */
        fun createLineDataSet(
            entries: List<Entry>,
            label: String,
            color: Int
        ): LineDataSet {
            return LineDataSet(entries, label).apply {
                setColor(color)
                setCircleColor(color)
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(true)
                valueTextSize = 10f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
        }
    }
}
