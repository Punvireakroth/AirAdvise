package com.example.airadvise.fragments

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.android.gms.maps.model.MapStyleOptions

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.airadvise.databinding.FragmentMapBinding
import com.example.airadvise.databinding.BottomSheetCityInfoBinding
import com.example.airadvise.models.City
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.Pollutant
import com.example.airadvise.models.PollutantType
import com.example.airadvise.api.ApiService
import com.example.airadvise.database.AppDatabase
import com.example.airadvise.database.CityDao
import com.example.airadvise.utils.PreferenceManager
import com.example.airadvise.utils.PollutantColorUtils
import com.example.airadvise.api.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.material.bottomsheet.BottomSheetBehavior

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.airadvise.R

import com.example.airadvise.models.request.FavoriteCityRequest
import com.example.airadvise.models.response.MapAirQualityResponse
import java.net.URL
import android.content.res.Resources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.airadvise.utils.Resource
import com.example.airadvise.utils.safeApiCall

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private lateinit var apiService: ApiService
    private lateinit var cityDao: CityDao
    private var currentPollutant = PollutantType.AQI
    private var currentCity: City? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var bottomSheetBinding: BottomSheetCityInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)

            insets
        }

        // Initialize dependencies
        apiService = ApiClient.createApiService(requireContext())
        cityDao = AppDatabase.getDatabase(requireContext()).cityDao()

        // Initialize the bottom sheet binding
        bottomSheetBinding = BottomSheetCityInfoBinding.bind(binding.bottomSheet.root)

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Check if we have a city ID from navigation arguments
        arguments?.getString("cityId")?.let { cityId ->
            Log.d("MapFragment", "Received cityId: $cityId")
            // Load city by ID - we'll load this after map is ready
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Try to get from database first
                    val cityEntity = cityDao.getCityById(cityId)
                    if (cityEntity != null) {
                        // Convert to City model
                        val city = City(
                            id = cityEntity.id,
                            name = cityEntity.name,
                            country = cityEntity.country,
                            latitude = cityEntity.latitude,
                            longitude = cityEntity.longitude,
                            isFavorite = cityEntity.isFavorite,
                            lastSearched = cityEntity.lastSearched
                        )
                        withContext(Dispatchers.Main) {
                            Log.d("MapFragment", "Found city in database: ${city.name}")
                            // Wait for map to be ready
                            if (::map.isInitialized) {
                                updateSelectedCity(city)
                            } else {
                                // Save city to use when map is ready
                                currentCity = city
                            }
                        }
                    } else {
                        // If not in database, try API
                        withContext(Dispatchers.Main) {
                            loadCityFromApi(cityId)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MapFragment", "Error loading city from database", e)
                        loadCityFromApi(cityId)
                    }
                }
            }
        }

        // Setup bottom sheet
        setupBottomSheet()

        // Now use bottomSheetBinding to access the views in the bottom sheet
        // Setup city search button
        bottomSheetBinding.searchButton.setOnClickListener {
            navigateToSearch()
        }

        // Setup menu button
        bottomSheetBinding.menuButton.setOnClickListener {
            showCityOptions()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Configure map settings
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        // Request location permission if needed
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }

        // Set map style
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.e("MapFragment", "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapFragment", "Can't find style. Error: ", e)
        }

        // If we already have a city from arguments, use it
        if (currentCity != null) {
            Log.d("MapFragment", "Using saved city: ${currentCity?.name}")
            updateSelectedCity(currentCity!!)
        } else {
            // Otherwise load last city or get current location
            loadLastCity()
        }

        // Setup map click listener
        map.setOnMapClickListener { latLng ->
            fetchCityAtLocation(latLng.latitude, latLng.longitude)
        }
    }

    private fun setupBottomSheet() {
        val bottomSheetView = binding.bottomSheet.root
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Handle state changes
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Handle slide events
            }
        })
    }


    private fun updatePollutantType(type: PollutantType) {
        currentPollutant = type
        currentCity?.let { loadAirQualityMap(it) }
    }


    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    fetchCityAtLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun loadLastCity() {
        lifecycleScope.launch(Dispatchers.Main) {
            val lastCityId = withContext(Dispatchers.IO) {
                PreferenceManager.getInstance(requireContext()).getLastViewedCityId()
            }

            if (lastCityId != null) {
                try {
                    // Try to get from database first
                    val cityEntity = withContext(Dispatchers.IO) {
                        cityDao.getCityById(lastCityId)
                    }

                    if (cityEntity != null) {
                        // Convert to City model
                        val city = City(
                            id = cityEntity.id,
                            name = cityEntity.name,
                            country = cityEntity.country,
                            latitude = cityEntity.latitude,
                            longitude = cityEntity.longitude,
                            isFavorite = cityEntity.isFavorite,
                            lastSearched = cityEntity.lastSearched
                        )
                        updateSelectedCity(city)
                    } else {
                        // If not in database, try API
                        loadCityFromApi(lastCityId)
                    }
                } catch (e: Exception) {
                    getCurrentLocation()
                }
            } else {
                getCurrentLocation()
            }
        }
    }

    private fun fetchCityAtLocation(latitude: Double, longitude: Double) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d("MapFragment", "Fetching city at location: $latitude, $longitude")
                val response = apiService.searchCities("nearby:$latitude,$longitude")
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val cities = response.body()?.data ?: emptyList()
                    if (cities.isNotEmpty()) {
                        Log.d("MapFragment", "Found city: ${cities.first().name}")
                        updateSelectedCity(cities.first())
                    } else {
                        Log.e("MapFragment", "No cities found")
                        Toast.makeText(requireContext(), "No cities found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("MapFragment", "API call failed: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "Error finding city: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("MapFragment", "Exception in fetchCityAtLocation", e)
                Toast.makeText(
                    requireContext(),
                    "Error finding city: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateSelectedCity(city: City) {
        currentCity = city

        // Save as last viewed city
        lifecycleScope.launch(Dispatchers.IO) {
            PreferenceManager.getInstance(requireContext()).saveLastViewedCityId(city.id)

            // Update city in database with last searched timestamp - MOVE TO IO DISPATCHER
            cityDao.updateLastSearched(city.id, System.currentTimeMillis())

            // Now update UI on main thread
            withContext(Dispatchers.Main) {
                // Move camera to city
                val latLng = LatLng(city.latitude, city.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))

                // Update bottom sheet
                updateCityInfo(city)

                // Load air quality data
                loadAirQualityData(city)

                // Load air quality map
                loadAirQualityMap(city)
            }
        }
    }

    private fun updateCityInfo(city: City) {
        bottomSheetBinding.cityNameText.text = city.name

        // Check if city is favorite - RUN ON IO THREAD
        lifecycleScope.launch(Dispatchers.IO) {
            val cityEntity = cityDao.getCityById(city.id)

            // Update UI on main thread
            withContext(Dispatchers.Main) {
                bottomSheetBinding.favoriteButton.isSelected = cityEntity?.isFavorite == true
            }
        }

        // Setup favorite button
        bottomSheetBinding.favoriteButton.setOnClickListener {
            toggleFavorite(city)
        }
    }

    private fun toggleFavorite(city: City) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isFavorite = !(cityDao.getCityById(city.id)?.isFavorite ?: false)

            try {
                if (isFavorite) {
                    apiService.addFavoriteCity(FavoriteCityRequest(city.id))
                } else {
                    apiService.removeFavoriteCity(city.id)
                }

                // Update local database
                cityDao.updateFavoriteStatus(city.id, isFavorite)

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    bottomSheetBinding.favoriteButton.isSelected = isFavorite

                    // Show confirmation
                    val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error updating favorites: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadAirQualityData(city: City) {

        Log.d("MapFragment", city.id);


        bottomSheetBinding.aqiProgressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = safeApiCall {
                    apiService.getCityAirQuality(city.id)
                }

                bottomSheetBinding.aqiProgressBar.visibility = View.GONE

                when (response) {
                    is Resource.Success -> {
                        val airQualityData = response.data!!
                        updateAirQualityInfo(airQualityData)
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Resource.Loading -> {
                        // Handle loading state
                    }
                }
            } catch (e: Exception) {
                bottomSheetBinding.aqiProgressBar.visibility = View.GONE
                Log.e("MapFragment", "Error loading air quality: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Error loading air quality: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateAirQualityInfo(data: AirQualityData) {
        bottomSheetBinding.aqiValueText.text = data.aqi.toString()

        // Update AQI level color
        val aqiColor = PollutantColorUtils.getColorForAQI(data.aqi)
        bottomSheetBinding.aqiValueText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                aqiColor
            )
        )

        // Update live indicator
        binding.liveIndicator.visibility = if (data.isLive) View.VISIBLE else View.GONE

        // Update pollutant values
        updatePollutantValues(data.pollutants)
    }

    private fun updatePollutantValues(pollutants: Map<PollutantType, Pollutant>) {
        // Use bottomSheetBinding for these views
        pollutants[PollutantType.NO2]?.let { no2 ->
            bottomSheetBinding.no2ValueText.text = "${no2.value} ${no2.unit}"
            bottomSheetBinding.no2ValueText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    no2.level.color
                )
            )
        }

        pollutants[PollutantType.PM25]?.let { pm25 ->
            bottomSheetBinding.pm25ValueText.text = "${pm25.value} ${pm25.unit}"
            bottomSheetBinding.pm25ValueText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    pm25.level.color
                )
            )
        }

        pollutants[PollutantType.PM10]?.let { pm10 ->
            bottomSheetBinding.pm10ValueText.text = "${pm10.value} ${pm10.unit}"
            bottomSheetBinding.pm10ValueText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    pm10.level.color
                )
            )
        }

        pollutants[PollutantType.O3]?.let { o3 ->
            bottomSheetBinding.o3ValueText.text = "${o3.value} ${o3.unit}"
            bottomSheetBinding.o3ValueText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    o3.level.color
                )
            )
        }
    }

    private fun loadAirQualityMap(city: City) {
        // Show progress indicator
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d("MapDebug", "Loading air quality map for: ${city.name}")
                Log.d("MapDebug", "Latitude: ${city.latitude}, Longitude: ${city.longitude}")
                Log.d("MapDebug", "Zoom level: ${map.cameraPosition.zoom}")
                Log.d("MapDebug", "Pollutant type: ${currentPollutant.name}")

                val response = apiService.getMapAirQuality(
                    city.latitude,
                    city.longitude,
                    map.cameraPosition.zoom,
                    currentPollutant.name
                )

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val mapData = response.body()!!
                    Log.d("MapDebug", "Response successful! Map URL: ${mapData.mapUrl}")

                    // Add tile overlay to map
                    map.clear()
                    val tileProvider = object : UrlTileProvider(256, 256) {
                        override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                            val url = "${mapData.mapUrl}/$zoom/$x/$y"
                            Log.d("MapDebug", "Generated tile URL: $url")
                            return try {
                                URL(url)
                            } catch (e: Exception) {
                                Log.e("MapDebug", "Error creating URL: ${e.message}")
                                null
                            }
                        }
                    }

                    val tileOverlay = map.addTileOverlay(
                        TileOverlayOptions()
                            .tileProvider(tileProvider)
                            .transparency(0.3f)
                    )

                    Log.d("MapDebug", "Tile overlay added: ${tileOverlay != null}")

                    // Add city marker
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(city.latitude, city.longitude))
                            .title(city.name)
                    )

                    Log.d("MapDebug", "Marker added: ${marker != null}")

                    // Force a refresh of the map
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(city.latitude, city.longitude),
                            map.cameraPosition.zoom
                        )
                    )
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    Log.e("MapDebug", "API call failed with code: ${response.code()}")
                    Log.e("MapDebug", "Error body: $errorBody")
                    Toast.makeText(
                        requireContext(),
                        "Error loading map data: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("MapDebug", "Exception in loadAirQualityMap", e)
                Toast.makeText(
                    requireContext(),
                    "Error loading map data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToSearch() {
        findNavController().navigate(R.id.action_mapFragment_to_searchCityFragment)
    }

    private fun showCityOptions() {
        val popupMenu = PopupMenu(requireContext(), bottomSheetBinding.menuButton)
        popupMenu.menuInflater.inflate(R.menu.city_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_forecast -> {
                    currentCity?.let { navigateToForecast(it) }
                    true
                }

                R.id.action_view_details -> {
                    currentCity?.let { navigateToCityDetails(it) }
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun navigateToForecast(city: City) {
        val bundle = Bundle().apply {
            putString("cityId", city.id)
        }
        findNavController().navigate(R.id.action_mapFragment_to_forecastFragment, bundle)
    }

    private fun navigateToCityDetails(city: City) {
        val bundle = Bundle().apply {
            putString("cityId", city.id)
        }
        findNavController().navigate(R.id.action_mapFragment_to_cityDetailsFragment, bundle)
    }

    private fun loadCityFromApi(cityId: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                Log.d("MapFragment", "Loading city from API: $cityId")
                val response = apiService.getCityDetails(cityId)

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val city = response.body()!!
                    Log.d("MapFragment", "Successfully loaded city from API: ${city.name}")
                    // Wait for map to be ready
                    if (::map.isInitialized) {
                        updateSelectedCity(city)
                    } else {
                        // Save city to use when map is ready
                        currentCity = city
                    }
                } else {
                    Log.e("MapFragment", "Failed to load city from API: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "Could not find the selected city",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("MapFragment", "Error loading city from API", e)
                Toast.makeText(
                    requireContext(),
                    "Error loading city: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}