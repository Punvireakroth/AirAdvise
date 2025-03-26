package com.example.airadvise.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.airadvise.R
import com.example.airadvise.databinding.FragmentHomeBinding
import com.example.airadvise.utils.SessionManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSwipeRefresh()
        loadInitialData()
    }

    private fun setSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        // Load air quality data for current location
        // For now we just show a dummy data
        displayAirQualityData(MockData.getMockAirQualityData())
        displayLocationData(MockData.getMockLocation())
    }

    // When the user pulls down to refresh the data
    private fun refreshData() {
        if (isLoading) return

        isLoading = true
        binding.swipeRefresh.isRefreshing = true

        // TODO: Add API call to refresh data

        // Simulate network call
        lifecycleScope.launch {
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // Use mock data for now
                displayAirQualityData(MockData.getMockAirQualityData())
                displayLocationData(MockData.getMockLocation())
                
                binding.swipeRefresh.isRefreshing = false
                isLoading = false
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error refreshing data", e)
                binding.swipeRefresh.isRefreshing = false
                isLoading = false
            }
        }
    }

    private fun displayAirQualityData(airQualityData: com.example.airadvise.models.AirQualityData) {
        // TODO: Update UI with air quality data
    }

    private fun displayLocationData(locationData: com.example.airadvise.models.Location) {
        // TODO: Update UI with location data
        // Example:
        binding.tvLocationName.text = "Location: ${locationData.cityName}, ${locationData.country}"
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