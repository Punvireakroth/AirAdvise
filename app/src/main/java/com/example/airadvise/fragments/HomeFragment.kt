package com.example.airadvise.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airadvise.R
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.DialogPollutantDetailsBinding
import com.example.airadvise.databinding.FragmentHomeBinding
import com.example.airadvise.models.Activity
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.AirQualityForecast
import com.example.airadvise.models.Location
import com.example.airadvise.models.Pollutant
import com.example.airadvise.utils.AQIUtils
import com.example.airadvise.utils.AirQualityCache
import com.example.airadvise.utils.BestDayRecommendation
import com.example.airadvise.utils.LocationProvider
import com.example.airadvise.utils.Resource
import com.example.airadvise.utils.safeApiCall
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale.*
import java.util.TimeZone


class HomeFragment : Fragment() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private val TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isLoading = false
    private var locationUpdateJob: Job? = null
    private var forecasts: List<AirQualityForecast> = emptyList()
    private var defaultActivity = Activity(1, "Walking", "low", 90)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var locationProvider: LocationProvider

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationProvider = LocationProvider(requireContext())

        val greeting = when (java.time.LocalTime.now().hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        binding.tvUsername.text = "$greeting, welcome to AirAdvise"

        // Initialize location name with placeholder
        binding.tvLocationName.text = "📍 San Francisco"


        setSwipeRefresh()

        // Try to get the cache data first
        val cacheAirQualityData = AirQualityCache.getCachedAirQualityData(requireContext())
        if (cacheAirQualityData != null) {
            val airQualityData = cacheAirQualityData
            displayAirQualityData(airQualityData)

            if (hasLocationPermission()) {
                refreshDataSilently()
            }
        } else {
            if (hasLocationPermission()) {
                getCurrentLocation()
            } else {
                handleError(getString(R.string.location_permission_required))
            }
        }

        // Check if we have location permission
        if (hasLocationPermission()) {
            getCurrentLocation()
            fetchForecastData()
        } else {
            handleError(getString(R.string.location_permission_required))
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        setUpLoadingState()

        lifecycleScope.launch {
            try {
                val location = locationProvider.getLastLocation()

                if (location != null) {
                    binding.swipeRefresh.isRefreshing = false
                    fetchAirQualityData(location.latitude, location.longitude)
                } else {
                    setupErrorState(getString(R.string.location_not_found))
                }
            } catch (e: Exception) {
                setupErrorState(getString(R.string.location_error))
            }
        }
    }

    private fun fetchAirQualityData(latitude: Double, longitude: Double) {
        setUpLoadingState()

        lifecycleScope.launch {
            try {
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext())
                        .getCurrentAirQuality(latitude, longitude)
                }

                when (response) {
                    is Resource.Success -> {
                        binding.swipeRefresh.isRefreshing = false
                        val airQualityData = response.data!!.airQualityData
                        displayAirQualityData(airQualityData)

                        // Fetch forecast data after getting current air quality
                        fetchForecastData()

                        setupContentState()
                    }

                    is Resource.Error -> {
                        setupErrorState(response.message ?: getString(R.string.unknown_error))
                    }

                    is Resource.Loading -> {
                        setUpLoadingState()
                    }
                }
            } catch (e: Exception) {
                setupErrorState(e.message ?: getString(R.string.unknown_error))
            }
        }
    }


private fun fetchForecastData() {
    lifecycleScope.launch {
        try {
            var locationId: Long? = null
            
            val location = locationProvider.getLastLocation()
            if (location != null) {
                try {
                    val airQualityResponse = safeApiCall {
                        ApiClient.createApiService(requireContext())
                            .getCurrentAirQuality(
                                location.latitude,
                                location.longitude
                            )
                    }
                    
                    if (airQualityResponse is Resource.Success && airQualityResponse.data != null) {
                        val responseLocationId = airQualityResponse.data.airQualityData.locationId
                        
                        if (responseLocationId > 0) {
                            locationId = responseLocationId
                            Log.d(TAG, "Got valid locationId from API: $locationId")
                        } else {
                            Log.w(TAG, "Got invalid locationId from API: $responseLocationId")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting location ID: ${e.message}")
                }
            }
            
            // try get locationId from cache
            if (locationId == null || locationId <= 0) {
                val cachedData = AirQualityCache.getCachedAirQualityData(requireContext())
                if (cachedData != null && cachedData.locationId > 0) {
                    locationId = cachedData.locationId
                    Log.d(TAG, "Using locationId from cache: $locationId")
                } else {
                    locationId = 1L
                    Log.w(TAG, "No valid locationId found, using default: $locationId")
                }
            }
            
            Log.d(TAG, "Fetching forecasts with locationId: $locationId")
            
            // Get forecasts using this location ID
            val response = safeApiCall {
                ApiClient.createApiService(requireContext())
                    .getForecasts(locationId)
            }
            
            // Rest of the code remains the same
            when (response) {
                is Resource.Success -> {
                    forecasts = response.data?.forecasts ?: emptyList()
                    val bestDayFromApi = response.data?.bestDay
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (bestDayFromApi != null) {
                            updateBestDayRecommendation(bestDayFromApi)
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error fetching forecast: ${response.message}")
                }
                is Resource.Loading -> {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching forecast: ${e.message}")
        }
    }
}

// Add this overloaded method that takes a bestDay parameter
@RequiresApi(Build.VERSION_CODES.O)
private fun updateBestDayRecommendation(bestDay: AirQualityForecast) {
    binding.bestDayCard.visibility = View.VISIBLE
    
    // Set best day details
    binding.tvBestDay.text = bestDay.getFormattedDate()
    binding.tvBestDayAqi.text = "AQI: ${bestDay.aqi}"
    
    // Set color based on AQI
    binding.tvBestDayAqi.backgroundTintList = android.content.res.ColorStateList.valueOf(
        bestDay.getCategoryColor()
    )
    
    // Set description
    binding.tvBestDayDescription.text = bestDay.description ?: "Best day for outdoor activities."
    
    // Make the best day card clickable to show details
    binding.bestDayCard.setOnClickListener {
        showBestDayDetailsDialog(bestDay)
    }
}

// Create a dialog to show detailed information
private fun showBestDayDetailsDialog(bestDay: AirQualityForecast) {
    val dialogBuilder = AlertDialog.Builder(requireContext())
    val dialogView = layoutInflater.inflate(R.layout.dialog_best_day_details, null)
    dialogBuilder.setView(dialogView)
    
    // Basic info
    val tvDialogDate = dialogView.findViewById<TextView>(R.id.tvDialogDate)
    val tvDialogAqi = dialogView.findViewById<TextView>(R.id.tvDialogAqi)
    val tvDialogCategory = dialogView.findViewById<TextView>(R.id.tvDialogCategory)
    val tvDialogDescription = dialogView.findViewById<TextView>(R.id.tvDialogDescription)
    val tvDialogRecommendation = dialogView.findViewById<TextView>(R.id.tvDialogRecommendation)
    
    // Pollutant values
    val tvPM25Value = dialogView.findViewById<TextView>(R.id.tvPM25Value)
    val tvPM10Value = dialogView.findViewById<TextView>(R.id.tvPM10Value)
    val tvO3Value = dialogView.findViewById<TextView>(R.id.tvO3Value)
    val tvNO2Value = dialogView.findViewById<TextView>(R.id.tvNO2Value)
    val tvSO2Value = dialogView.findViewById<TextView>(R.id.tvSO2Value)
    val tvCOValue = dialogView.findViewById<TextView>(R.id.tvCOValue)
    
    val llActivitiesList = dialogView.findViewById<LinearLayout>(R.id.llActivitiesList)
    
    // Set values
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        tvDialogDate.text = bestDay.getFormattedDate()
    } else {
        tvDialogDate.text = bestDay.forecastDate ?: "Unknown date"
    }
    
    tvDialogAqi.text = "AQI: ${bestDay.aqi}"
    tvDialogAqi.backgroundTintList = android.content.res.ColorStateList.valueOf(
        bestDay.getCategoryColor()
    )
    
    tvDialogCategory.text = bestDay.category
    tvDialogDescription.text = bestDay.description
    tvDialogRecommendation.text = bestDay.recommendation
    
    // Set pollutant values
    tvPM25Value.text = "${bestDay.pm25} μg/m³"
    tvPM10Value.text = "${bestDay.pm10} μg/m³"
    tvO3Value.text = "${bestDay.o3} ppb"
    tvNO2Value.text = "${bestDay.no2} ppm"
    tvSO2Value.text = "${bestDay.so2} ppm"
    tvCOValue.text = "${bestDay.co} ppb"
    
    // Get activities based on category
    val recommendedActivities = bestDay.recommendedActivities
    if (recommendedActivities != null) {
        // Determine which activity list to show based on category
        val activityList = when (bestDay.category.lowercase()) {
            "good" -> recommendedActivities.high ?: recommendedActivities.moderate ?: recommendedActivities.low
            "moderate" -> recommendedActivities.moderate ?: recommendedActivities.low
            else -> recommendedActivities.low
        }
        
        // Add activities to the list
        if (activityList != null && activityList.isNotEmpty()) {
            dialogView.findViewById<TextView>(R.id.tvRecommendedActivitiesTitle).visibility = View.VISIBLE
            llActivitiesList.removeAllViews() // Clear any existing views
            
            for (activity in activityList) {
                val activityView = TextView(requireContext()).apply {
                    text = "• $activity"
                    setPadding(0, 8, 0, 8)
                    textSize = 14f
                }
                llActivitiesList.addView(activityView)
            }
        } else {
            dialogView.findViewById<TextView>(R.id.tvRecommendedActivitiesTitle).visibility = View.GONE
        }
    } else {
        dialogView.findViewById<TextView>(R.id.tvRecommendedActivitiesTitle).visibility = View.GONE
    }
    
    // Create and show dialog
    val dialog = dialogBuilder.create()
    
    // Add close button action
    dialogView.findViewById<Button>(R.id.btnDialogClose).setOnClickListener {
        dialog.dismiss()
    }
    
    dialog.show()
}
    private fun displayAirQualityData(airQualityData: AirQualityData) {
        val detailsBinding = binding.airQualityDetails

        // Set AQI value in gauge
        detailsBinding.aqiGaugeView.setAQI(airQualityData.aqi)

        // Set updated time
        detailsBinding.tvUpdatedTime.text = "Updated at ${formatTimestamp(airQualityData.timestamp)}"

        // Set health implications and precautions
        detailsBinding.tvHealthImplications.text =
            AQIUtils.getHealthImplications(requireContext(), airQualityData.aqi)
        detailsBinding.tvPrecautions.text =
            AQIUtils.getPrecautions(requireContext(), airQualityData.aqi)
    }

    private fun displayLocationData(location: Location) {
        // TODO: To implement
    }

    private fun formatTimestamp(timestamp: String): String {
        // Format timestamp string to user-friendly format
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return timestamp

            val outputFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", getDefault())
            return outputFormat.format(date)
        } catch (e: Exception) {
            return timestamp
        }
    }

    private fun setSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Check if we have location permission
            if (hasLocationPermission()) {
                // Get current location again
                lifecycleScope.launch {
                    try {
                        val location = locationProvider.getLastLocation()

                        if (location != null) {
                            fetchAirQualityData(location.latitude, location.longitude)
                            fetchForecastData() // Also refresh forecast data
                        } else {
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.location_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(
                            requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_permission_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Refresh without the loading animation
    private fun refreshDataSilently() {
        lifecycleScope.launch {
            try {
                val location = locationProvider.getLastLocation() ?: return@launch

                val response = safeApiCall {
                    ApiClient.createApiService(requireContext())
                        .getCurrentAirQuality(location.latitude, location.longitude)
                }

                if (response is Resource.Success && response.data != null) {
                    binding.swipeRefresh.isRefreshing = false

                    val airQualityData = response.data.airQualityData
                    AirQualityCache.saveAirQualityDataWithLocation(
                        requireContext(),
                        airQualityData
                    )

                    displayAirQualityData(airQualityData)

                    // Also refresh forecast data
                    fetchForecastData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh failed: ${e.message}")
            }
        }
    }

    private fun startLocationUpdates() {
        // Cancel existing job if any
        locationUpdateJob?.cancel()

        if (!hasLocationPermission()) return

        locationUpdateJob = lifecycleScope.launch {
            try {
                locationProvider.getLocationUpdates(30000) // Updates every 30 seconds
                    .collect { location ->
                        // Only refresh if not already refreshing and we detect significant movement
                        if (!isLoading && !binding.swipeRefresh.isRefreshing) {
                            fetchAirQualityData(location.latitude, location.longitude)
                        }
                    }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Location updates error: ${e.message}")
            }
        }
    }

    // Loading and error state
    private fun setUpLoadingState() {
        binding.loadingState.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
        binding.errorState.visibility = View.GONE
    }

    private fun setupContentState() {
        binding.loadingState.visibility = View.GONE
        binding.contentContainer.visibility = View.VISIBLE
        binding.errorState.visibility = View.GONE
    }

    private fun setupErrorState(message: String) {
        binding.loadingState.visibility = View.GONE
        binding.contentContainer.visibility = View.GONE
        binding.errorState.visibility = View.VISIBLE
        binding.errorState.findViewById<TextView>(R.id.tvErrorMessage).text = message

        // Set up retry button
        binding.errorState.findViewById<Button>(R.id.btnRetry).setOnClickListener {
            if (hasLocationPermission()) {
                getCurrentLocation()
            } else {
                // Request location permission
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            ), LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun handleError(errorMessage: String) {
        binding.swipeRefresh.isRefreshing = false
        isLoading = false

        // Try to use cached data first if available
        val cachedData = AirQualityCache.getCachedAirQualityData(requireContext())
        if (cachedData != null) {
            // We have cached data, so just show a toast with the error
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

            // Display the cached data
            val airQualityData = cachedData
            displayAirQualityData(airQualityData)
            binding.contentContainer.visibility = View.VISIBLE
        } else {
            // No cached data, show the error state
            binding.contentContainer.visibility = View.GONE

            // Show the error layout
            val errorView = binding.errorState
            errorView.visibility = View.VISIBLE
            errorView.findViewById<TextView>(R.id.tvErrorMessage).text = errorMessage

            // Setup retry button
            errorView.findViewById<Button>(R.id.btnRetry).setOnClickListener {
                errorView.visibility = View.GONE
                if (hasLocationPermission()) {
                    getCurrentLocation()
                } else {
                    // Still need location permission
                    handleError(getString(R.string.location_permission_required))
                }
            }
        }
    }


    private fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    override fun onDestroyView() {
        stopLocationUpdates()
        super.onDestroyView()
        _binding = null
    }
}