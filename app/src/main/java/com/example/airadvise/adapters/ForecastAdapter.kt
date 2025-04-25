package com.example.airadvise.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.airadvise.databinding.ItemForecastBinding
import com.example.airadvise.models.AirQualityForecast

class ForecastAdapter(
    private val context: Context,
    private var forecasts: List<AirQualityForecast> = emptyList(),
    private val onItemClick: (AirQualityForecast) -> Unit
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    fun updateForecasts(newForecasts: List<AirQualityForecast>) {
        forecasts = newForecasts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecasts[position]
        holder.bind(forecast)
    }

    override fun getItemCount(): Int = forecasts.size

    inner class ForecastViewHolder(private val binding: ItemForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(forecast: AirQualityForecast) {
            with(binding) {
                tvDay.text = forecast.getFormattedDate()
                tvAqi.text = forecast.aqi.toString()
                tvCategory.text = forecast.category
                tvCategory.setBackgroundColor(forecast.getCategoryColor())

                // Set pollutant values if available
                tvPm25.text = forecast.pm25?.let { "PM2.5: $it" } ?: "PM2.5: --"
                tvPm10.text = forecast.pm10?.let { "PM10: $it" } ?: "PM10: --"
                tvO3.text = forecast.o3?.let { "O₃: $it" } ?: "O₃: --"

                // Set click listener for the entire item
                root.setOnClickListener {
                    onItemClick(forecast)
                }

                // Set click listener for the "See Details" text
                tvSeeDetails.setOnClickListener {
                    onItemClick(forecast)
                }
            }
        }
    }
}
