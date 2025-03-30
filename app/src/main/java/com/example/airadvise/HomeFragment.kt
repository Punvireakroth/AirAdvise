package com.example.airadvise.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airadvise.R
import com.example.airadvise.adapters.PollutantAdapter
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.DialogPollutantDetailsBinding
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.Pollutant
import com.example.airadvise.utils.AQIUtils
import com.example.airadvise.utils.LocationProvider
import com.example.airadvise.utils.safeApiCall
import com.example.airadvise.utils.Resource
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Locale.*
import java.util.TimeZone


class HomeFragment : Fragment() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var locationProvider: LocationProvider
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationProvider = LocationProvider(requireContext())

        setSwipeRefresh()
//        loadInitialData()
        // Check if we have location permission
        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            handleError(getString(R.string.location_permission_required))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        setUpLoadingState()

        lifecycleScope.launch {
            try {
                val location = locationProvider.getLastLocation()

                if (location != null) {
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
                        val airQualityData = response.data!!.airQualityData
                        displayAirQualityData(airQualityData)
                        
                        // TODO: Handle these data in the future tasks phases
                        // response.data.safeActivities
                        // response.data.unsafeActivities
                        // response.data.healthTips
                        
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

    private fun displayAirQualityData(airQualityData: AirQualityData) {
        val detailsBinding = binding.airQualityDetails

        // Set AQI value in gauge
        detailsBinding.aqiGaugeView.setAQI(airQualityData.aqi)

        // Set updated time
        detailsBinding.tvUpdatedTime.text = getString(R.string.updated_at, formatTimestamp(airQualityData.timestamp))

        // Set health implications and precautions
        detailsBinding.tvHealthImplications.text = AQIUtils.getHealthImplications(requireContext(), airQualityData.aqi)
        detailsBinding.tvPrecautions.text = AQIUtils.getPrecautions(requireContext(), airQualityData.aqi)

        // Setup pollutants recycler view
        val pollutantAdapter = PollutantAdapter(airQualityData.getPollutants()) { pollutant ->
            // Handle pollutant click - show details dialog
            showPollutantDetailsDialog(pollutant)
        }

        detailsBinding.rvPollutants.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pollutantAdapter
        }
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
                            requireContext(),
                            getString(R.string.location_error),
                            Toast.LENGTH_SHORT
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

    private fun showPollutantDetailsDialog(pollutant: Pollutant) {
        val dialogBinding = DialogPollutantDetailsBinding.inflate(layoutInflater)

        dialogBinding.tvPollutantName.text = pollutant.name
        dialogBinding.tvPollutantValue.text = "${pollutant.value} ${pollutant.unit}"
        dialogBinding.tvPollutantDescription.text = pollutant.description

        // Set progress based on pollutant index
        dialogBinding.progressBar.progress = pollutant.index

        // Set color based on index
        dialogBinding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
            AQIUtils.getAQIColor(requireContext(), pollutant.index)
        )

        // Set health impact information
        dialogBinding.tvHealthImpact.text = getHealthImpactForPollutant(pollutant.code)

        // Create and show dialog
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.learn_more) { _, _ ->
                navigateToPollutantEducation(pollutant.code)
            }
            .create()
            .show()
    }

    private fun getHealthImpactForPollutant(pollutantCode: String): String {
        return when (pollutantCode) {
            "pm25" -> getString(R.string.pm25_health_impact)
            "pm10" -> getString(R.string.pm10_health_impact)
            "o3" -> getString(R.string.o3_health_impact)
            "no2" -> getString(R.string.no2_health_impact)
            "so2" -> getString(R.string.so2_health_impact)
            "co" -> getString(R.string.co_health_impact)
            else -> getString(R.string.general_pollutant_impact)
        }
    }

    private fun navigateToPollutantEducation(pollutantCode: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.coming_soon_educational_content),
            Toast.LENGTH_SHORT
        ).show()
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
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun handleError(errorMessage: String) {
        binding.swipeRefresh.isRefreshing = false
        isLoading = false

        // Show error message
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}