package com.example.airadvise.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.airadvise.R
import com.example.airadvise.adapters.CitySearchAdapter
import com.example.airadvise.adapters.AlphabetIndexAdapter
import com.example.airadvise.api.ApiClient
import com.example.airadvise.api.ApiService
import com.example.airadvise.database.AppDatabase
import com.example.airadvise.database.CityDao
import com.example.airadvise.database.CityEntity
import com.example.airadvise.database.toCity
import com.example.airadvise.database.toEntity
import com.example.airadvise.databinding.FragmentSearchCityBinding
import com.example.airadvise.models.City
import com.example.airadvise.utils.PreferenceManager

import kotlinx.coroutines.launch

class SearchCityFragment : Fragment() {
    
    private lateinit var binding: FragmentSearchCityBinding
    private lateinit var apiService: ApiService
    private lateinit var cityDao: CityDao
    private lateinit var searchAdapter: CitySearchAdapter
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchCityBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize dependencies
        apiService = ApiClient.createApiService(requireContext())
        cityDao = AppDatabase.getDatabase(requireContext()).cityDao()
        
        // Setup search adapter
        setupSearchAdapter()
        
        // Setup search input
        setupSearchInput()
        
        // Setup alphabet index
        setupAlphabetIndex()
        
        // Load recent searches
        loadRecentSearches()
        
        // Setup cancel button
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupSearchAdapter() {
        searchAdapter = CitySearchAdapter { city ->
            selectCity(city)
        }
        
        binding.cityRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }
    
    private fun setupSearchInput() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel previous search if any
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                // Create new search with delay
                searchRunnable = Runnable {
                    searchCities(s.toString())
                }
                
                // Post delayed search
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Focus search input and show keyboard
        binding.searchEditText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun setupAlphabetIndex() {
        val alphabet = ('A'..'Z').toList()
        val indexAdapter = AlphabetIndexAdapter(alphabet) { letter ->
            // Scroll to section
            scrollToLetter(letter)
        }
        
        binding.alphabetRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = indexAdapter
        }
    }
    
    private fun scrollToLetter(letter: Char) {
        val position = searchAdapter.getPositionForLetter(letter)
        if (position != -1) {
            binding.cityRecyclerView.scrollToPosition(position)
        }
    }
    
    private fun searchCities(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.noResultsText.visibility = View.GONE
        
        if (query.isBlank()) {
            loadRecentSearches()
            binding.progressBar.visibility = View.GONE
            return
        }
        
        lifecycleScope.launch {
            try {
                // Use safeApiCall if needed, similar to other fragments
                val response = apiService.searchCities(query)
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val cities = response.body() ?: emptyList()
                    
                    if (cities.isEmpty()) {
                        binding.noResultsText.visibility = View.VISIBLE
                    } else {
                        binding.noResultsText.visibility = View.GONE
                    }
                    
                    // Update favorite status from local database
                    val updatedCities = cities.map { city ->
                        try {
                            val localCity = cityDao.getCityById(city.id)
                            city.copy(isFavorite = localCity?.isFavorite ?: false)
                        } catch (e: Exception) {
                            // If there's an error, just return the original city
                            city
                        }
                    }
                    
                    searchAdapter.submitList(updatedCities)
                } else {
                    Toast.makeText(requireContext(), "Error searching cities", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadRecentSearches() {
        lifecycleScope.launch {
            try {
                // Get entities from database
                val entities: List<CityEntity> = cityDao.getRecentSearches()
                
                // Convert entities to City objects explicitly
                val recentCities: List<City> = entities.map { entity ->
                    City(
                        id = entity.id,
                        name = entity.name,
                        country = entity.country,
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        isFavorite = entity.isFavorite,
                        lastSearched = entity.lastSearched
                    )
                }
                
                if (recentCities.isEmpty()) {
                    binding.recentSearchesLabel.visibility = View.GONE
                } else {
                    binding.recentSearchesLabel.visibility = View.VISIBLE
                    searchAdapter.submitList(recentCities)
                }
            } catch (e: Exception) {
                // Handle error
                binding.recentSearchesLabel.visibility = View.GONE
            }
        }
    }
    
    private fun selectCity(city: City) {
        // Update last searched timestamp
        lifecycleScope.launch {
            try {
                // Create entity directly instead of using the extension function
                val entity = CityEntity(
                    id = city.id,
                    name = city.name,
                    country = city.country,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    isFavorite = city.isFavorite,
                    lastSearched = System.currentTimeMillis()
                )
                
                // Save to database
                cityDao.insertCity(entity)
                
                // Save as last viewed city
                PreferenceManager.getInstance(requireContext()).saveLastViewedCityId(city.id)
                
                // Navigate back to map with selected city
                val bundle = Bundle().apply {
                    putString("cityId", city.id)
                }
                findNavController().navigate(R.id.action_searchCityFragment_to_mapFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error saving city: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 