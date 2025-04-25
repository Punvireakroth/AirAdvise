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
import com.example.airadvise.adapters.PollutantAdapter
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

        setSwipeRefresh()
        
        // Setup best day view button
        binding.btnViewForecasts.setOnClickListener {
            // Navigate to forecasts fragment
            // Implement navigation to ForecastFragment when ready
            Toast.makeText(requireContext(), "Forecast view coming soon", Toast.LENGTH_SHORT).show()
        }

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
                    ApiClient.createApiService(requireContext()).getCurrentAirQuality(latitude, longitude)
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
    
    // Fetch forecast data from API - use similar approach to ForecastFragment
    private fun fetchForecastData() {
        lifecycleScope.launch {
            try {
                var locationId = 1L
                
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
                        
                        if (airQualityResponse is Resource.Success) {
                            locationId = airQualityResponse.data?.airQualityData?.locationId ?: 1L
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting location ID: ${e.message}")
                    }
                }
                
                // Now get forecasts using this location ID
                val response = safeApiCall {
                    ApiClient.createApiService(requireContext())
                        .getForecasts(locationId)
                }
                
                when (response) {
                    is Resource.Success -> {
                        forecasts = response.data?.forecasts ?: emptyList()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            updateBestDayRecommendation()
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error fetching forecast: ${response.message}")
                    }
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching forecast: ${e.message}")
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateBestDayRecommendation() {
        if (forecasts.isEmpty()) {
            binding.bestDayCard.visibility = View.GONE
            return
        }
        
        // Get best day for default activity (walking)
        val bestDay = BestDayRecommendation.getBestDayForActivity(forecasts, defaultActivity)
        
        if (bestDay != null) {
            binding.bestDayCard.visibility = View.VISIBLE
            
            // Set best day details
            binding.tvBestDay.text = bestDay.getFormattedDate()
            binding.tvBestDayAqi.text = "AQI: ${bestDay.aqi}"
            
            // Set color based on AQI
            binding.tvBestDayAqi.backgroundTintList = android.content.res.ColorStateList.valueOf(
                bestDay.getCategoryColor()
            )
            
            // Set description
            binding.tvBestDayDescription.text = if (BestDayRecommendation.isDaySuitableForActivity(bestDay, defaultActivity)) {
                "This day is suitable for outdoor activities with ${bestDay.category} air quality."
            } else {
                "This is the best available day, but caution is advised as air quality is ${bestDay.category.lowercase()}."
            }
        } else {
            binding.bestDayCard.visibility = View.GONE
        }
    }

    private fun displayAirQualityData(airQualityData: AirQualityData) {
        val detailsBinding = binding.airQualityDetails

        // Set AQI value in gauge
        detailsBinding.aqiGaugeView.setAQI(airQualityData.aqi)

        // Set updated time
        detailsBinding.tvUpdatedTime.text = "Updated at"

        // Set health implications and precautions
        detailsBinding.tvHealthImplications.text =
            AQIUtils.getHealthImplications(requireContext(), airQualityData.aqi)
        detailsBinding.tvPrecautions.text =
            AQIUtils.getPrecautions(requireContext(), airQualityData.aqi)
        // Hide recycler view
        detailsBinding.rvPollutants?.visibility = View.GONE
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