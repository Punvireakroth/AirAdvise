// app/src/main/java/com/example/airadvise/ForecastFragment.kt
package com.example.airadvise.fragments

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airadvise.R
import com.example.airadvise.adapters.ForecastAdapter
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.DialogForecastDetailsBinding
import com.example.airadvise.databinding.DialogLocationSelectionBinding
import com.example.airadvise.databinding.FragmentForecastBinding
import com.example.airadvise.models.AirQualityForecast
import com.example.airadvise.utils.safeApiCall
import com.example.airadvise.models.Location as LocationModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.IOException
import com.example.airadvise.utils.Resource

class ForecastFragment : Fragment() {
    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var forecasts: List<AirQualityForecast> = emptyList()
    private var savedLocations: List<LocationModel> = emptyList()
    private var currentLocation: android.location.Location? = null
    private var selectedLocation: LocationModel? = null
    private var currentPollutantFilter = "AQI"
    private var isLoading = false
    private var lastKnownLocationId: Long? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupRecyclerView()
        setupSwipeRefresh()
        setupPollutantFilters()
        setupLocationSelection()

        // Request location permission and load data
        checkLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        forecastAdapter = ForecastAdapter(requireContext(), emptyList()) { forecast ->
            showForecastDetails(forecast)
        }

        binding.forecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forecastAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadForecastData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPollutantFilters() {
        binding.pollutantFilterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentPollutantFilter = when (checkedId) {
                R.id.chipAqi -> "AQI"
                R.id.chipPm25 -> "PM2.5"
                R.id.chipPm10 -> "PM10"
                R.id.chipO3 -> "O₃"
                R.id.chipNo2 -> "NO₂"
                R.id.chipSo2 -> "SO₂"
                R.id.chipCo -> "CO"
                else -> "AQI"
            }

            // Update chart with selected pollutant
            updateChart()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupLocationSelection() {
        binding.btnSelectLocation.setOnClickListener {
            showLocationSelectionDialog()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                getCurrentLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show explanation why we need location
                Toast.makeText(
                    requireContext(),
                    "Location permission is needed to show air quality forecast for your area",
                    Toast.LENGTH_LONG
                ).show()
                requestLocationPermission()
            }

            else -> {
                // No explanation needed, request the permission
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                getCurrentLocation()
            } else {
                // Permission denied
                Toast.makeText(
                    requireContext(),
                    "Location permission denied. Using default location.",
                    Toast.LENGTH_SHORT
                ).show()
                // Load data without location
                loadForecastData()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        showLoading(true)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            currentLocation = location
            loadForecastData()
        }.addOnFailureListener {
            showLoading(false)
            handleError("Failed to get location: ${it.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadForecastData() {
        if (isLoading) return
        
        isLoading = true
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("ForecastFragment", "Loading forecast. Selected location: $selectedLocation, Current location: $currentLocation")
                
                val forecasts = if (selectedLocation != null) {
                    val response = safeApiCall {
                        ApiClient.createApiService(requireContext())
                            .getForecasts(selectedLocation!!.id)
                    }
                    lastKnownLocationId = selectedLocation!!.id
                    
                    when (response) {
                        is Resource.Success -> response.data?.forecasts ?: emptyList()
                        is Resource.Error -> {
                            handleError(response.message ?: getString(R.string.unknown_error))
                            emptyList()
                        }
                        is Resource.Loading -> emptyList()
                    }
                } else if (currentLocation != null) {
                    // For current location, we need to get locationId first from air quality data
                    // Then use that ID to fetch forecasts
                    val airQualityResponse = safeApiCall {
                        ApiClient.createApiService(requireContext())
                            .getCurrentAirQuality(
                                currentLocation!!.latitude,
                                currentLocation!!.longitude
                            )
                    }
                    
                    when (airQualityResponse) {
                        is Resource.Success -> {
                            val locationId = 1L
                            
                            // Now get forecasts using this location ID
                            val forecastResponse = safeApiCall {
                                ApiClient.createApiService(requireContext())
                                    .getForecasts(locationId)
                            }
                            
                            when (forecastResponse) {
                                is Resource.Success -> forecastResponse.data?.forecasts ?: emptyList()
                                is Resource.Error -> {
                                    handleError(forecastResponse.message ?: getString(R.string.unknown_error))
                                    emptyList()
                                }
                                is Resource.Loading -> emptyList()
                            }
                        }
                        is Resource.Error -> {
                            handleError(airQualityResponse.message ?: getString(R.string.unknown_error))
                            emptyList()
                        }
                        is Resource.Loading -> emptyList()
                    }
                } else {
                    // use a default location ID
                    val forecastResponse = safeApiCall {
                        ApiClient.createApiService(requireContext())
                            .getForecasts(1L) 
                    }
                    
                    when (forecastResponse) {
                        is Resource.Success -> forecastResponse.data?.forecasts ?: emptyList()
                        is Resource.Error -> {
                            handleError(forecastResponse.message ?: getString(R.string.unknown_error))
                            emptyList()
                        }
                        is Resource.Loading -> emptyList()
                    }
                }
                
                this@ForecastFragment.forecasts = forecasts
                
                // Update UI
                requireActivity().runOnUiThread {
                    updateUI(forecasts)
                    showLoading(false)
                    isLoading = false
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    val detailedError = "Error: ${e.javaClass.simpleName} - ${e.message}"
                    android.util.Log.e("ForecastFragment", detailedError, e)
                    handleError(detailedError)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun handleForecastResponse(response: Resource<List<AirQualityForecast>>): List<AirQualityForecast> {
        return when (response) {
            is Resource.Success -> response.data ?: emptyList()
            is Resource.Error -> {
                handleError(response.message ?: getString(R.string.unknown_error))
                emptyList()
            }

            is Resource.Loading -> emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUI(forecasts: List<AirQualityForecast>) {
        if (forecasts.isEmpty()) {
            binding.errorView.root.visibility = View.VISIBLE
            binding.errorView.tvErrorMessage.text = "No forecast data available"
            binding.swipeRefreshLayout.visibility = View.GONE
            return
        }

        binding.errorView.root.visibility = View.GONE
        binding.swipeRefreshLayout.visibility = View.VISIBLE

        // Update adapter
        forecastAdapter.updateForecasts(forecasts)

        // Update chart
        updateChart()

        // Update location button text
        val locationText = selectedLocation?.cityName ?: "Current Location"
        binding.btnSelectLocation.text = locationText
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateChart() {
        if (forecasts.isEmpty()) return

        val entries = mutableListOf<Entry>()
        val xAxisLabels = mutableListOf<String>()

        // Prepare data based on the selected pollutant
        forecasts.forEachIndexed { index, forecast ->
            val value = when (currentPollutantFilter) {
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
            xAxisLabels.add(forecast.getShortDate())
        }

        // Create dataset
        val dataSet = LineDataSet(entries, currentPollutantFilter)
        dataSet.apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Create line data
        val lineData = LineData(dataSet)

        // Configure chart
        binding.forecastChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = true

            // X-axis configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                setDrawGridLines(false)
            }

            // Left Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            // Right Y-axis
            axisRight.isEnabled = false

            // Refresh chart
            invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showForecastDetails(forecast: AirQualityForecast) {
        val dialogBinding = DialogForecastDetailsBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        with(dialogBinding) {
            tvDetailDate.text = forecast.getFormattedDate()
            tvDetailAqi.text = forecast.aqi.toString()
            tvDetailCategory.text = forecast.category
            tvDetailCategory.setBackgroundColor(forecast.getCategoryColor())
            tvDetailDescription.text = forecast.description

            // Set pollutant values
            tvDetailPm25.text = forecast.pm25?.let { "$it μg/m³" } ?: "--"
            tvDetailPm10.text = forecast.pm10?.let { "$it μg/m³" } ?: "--"
            tvDetailO3.text = forecast.o3?.let { "$it ppb" } ?: "--"
            tvDetailNo2.text = forecast.no2?.let { "$it ppb" } ?: "--"
            tvDetailSo2.text = forecast.so2?.let { "$it ppb" } ?: "--"
            tvDetailCo.text = forecast.co?.let { "$it ppm" } ?: "--"

            // Set recommendation if available
            tvDetailRecommendation.text =
                forecast.recommendation ?: "No specific recommendations available for this day."

            // Close button
            btnClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLocationSelectionDialog() {
        val dialogBinding = DialogLocationSelectionBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        // Add saved locations to radio group
        savedLocations.forEach { location ->
            val radioButton = android.widget.RadioButton(context).apply {
                text = location.cityName
                id = View.generateViewId()
                tag = location.id
            }
            dialogBinding.rgLocationOptions.addView(radioButton)
        }

        // Handle location selection
        dialogBinding.btnSelectLocation.setOnClickListener {
            val selectedId = dialogBinding.rgLocationOptions.checkedRadioButtonId

            if (selectedId == dialogBinding.rbCurrentLocation.id) {
                // Current location selected
                selectedLocation = null
                getCurrentLocation()
            } else {
                // Saved location selected
                val selectedRadioButton =
                    dialog.findViewById<android.widget.RadioButton>(selectedId)
                val locationId = selectedRadioButton.tag as Long

                // Find the selected location
                selectedLocation = savedLocations.find { it.id == locationId }
                loadForecastData()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleError(errorMessage: String) {
        showLoading(false)
        isLoading = false

        // Show error view
        binding.errorView.root.visibility = View.VISIBLE
        binding.errorView.tvErrorMessage.text = errorMessage
        binding.swipeRefreshLayout.visibility = View.GONE

        // Log error
        android.util.Log.e("ForecastFragment", errorMessage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

