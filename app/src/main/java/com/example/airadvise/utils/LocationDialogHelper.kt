package com.example.airadvise.utils

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import com.example.airadvise.api.ApiClient
import com.example.airadvise.databinding.DialogLocationSelectionBinding
import com.example.airadvise.models.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class to manage location selection dialog functionality
 */
class LocationDialogHelper(
    private val context: Context,
    private val onLocationSelected: (Location?) -> Unit
) {
    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

//    fun showLocationSelectionDialog(savedLocations: List<Location>) {
//        val dialogBinding = DialogLocationSelectionBinding.inflate(LayoutInflater.from(context))
//        val dialog = Dialog(context)
//        dialog.setContentView(dialogBinding.root)
//
//        // Add saved locations to radio group
//        savedLocations.forEach { location ->
//            val radioButton = RadioButton(context).apply {
//                text = location.cityName
//                id = View.generateViewId()
//                tag = location
//            }
//            dialogBinding.rgLocationOptions.addView(radioButton)
//        }
//
//        // Setup search functionality
//        dialogBinding.etSearchLocation.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Cancel previous search job if any
//                searchJob?.cancel()
//
//                // Start new search with delay to avoid too many API calls
//                val searchText = s.toString().trim()
//                if (searchText.length >= 2) {
//                    searchJob = coroutineScope.launch {
//                        delay(500) // Debounce
//                        searchLocations(searchText, dialogBinding, dialog)
//                    }
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        // Handle selection button click
//        dialogBinding.btnSelectLocation.setOnClickListener {
//            val selectedId = dialogBinding.rgLocationOptions.checkedRadioButtonId
//
//            if (selectedId == dialogBinding.rbCurrentLocation.id) {
//                // Current location selected
//                onLocationSelected(null)
//            } else {
//                // Find the selected radio button
//                val radioButton = dialog.findViewById<RadioButton>(selectedId)
//
//                // Get the location from the tag
//                val location = radioButton.tag as? Location
//                onLocationSelected(location)
//            }
//
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }

//    private suspend fun searchLocations(
//        query: String,
//        binding: DialogLocationSelectionBinding,
//        dialog: Dialog
//    ) {
//        try {
//            val response = withContext(Dispatchers.IO) {
//                ApiClient.apiService.searchLocations(query)
//            }
//
//            if (response.isSuccessful) {
//                val locations = response.body()?.locations ?: emptyList()
//
//                // Clear previous search results (keep the Current Location option)
//                val count = binding.rgLocationOptions.childCount
//                if (count > 1) {
//                    binding.rgLocationOptions.removeViews(1, count - 1)
//                }
//
//                // Add search results
//                locations.forEach { location ->
//                    val radioButton = RadioButton(context).apply {
//                        text = "${location.name} (${location.country})"
//                        id = View.generateViewId()
//                        tag = location
//                    }
//                    binding.rgLocationOptions.addView(radioButton)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("LocationDialogHelper", "Error searching locations: ${e.message}")
//        }
//    }
}
