package com.example.airadvise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airadvise.R
import com.example.airadvise.models.City

class CitySearchAdapter(private val onCitySelected: (City) -> Unit) : 
    ListAdapter<City, CitySearchAdapter.CityViewHolder>(CityDiffCallback()) {
    
    // Map to store positions by first letter
    private val letterPositions = mutableMapOf<Char, Int>()
    
    override fun submitList(list: List<City>?) {
        super.submitList(list)
        
        // Update letter positions map
        letterPositions.clear()
        list?.forEachIndexed { index, city ->
            val firstLetter = city.name.first().uppercaseChar()
            if (!letterPositions.containsKey(firstLetter)) {
                letterPositions[firstLetter] = index
            }
        }
    }
    
    fun getPositionForLetter(letter: Char): Int {
        return letterPositions[letter] ?: -1
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityNameText: TextView = itemView.findViewById(R.id.cityNameText)
        private val countryText: TextView = itemView.findViewById(R.id.countryText)
        private val letterHeader: TextView = itemView.findViewById(R.id.letterHeader)
        private val indexLetter: TextView = itemView.findViewById(R.id.indexLetter)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCitySelected(getItem(position))
                }
            }
        }
        
        fun bind(city: City) {
            cityNameText.text = city.name
            countryText.text = city.country
            
            // Show first letter as section header if it's the first occurrence
            val position = adapterPosition
            if (position > 0) {
                val previousCity = getItem(position - 1)
                val currentFirstLetter = city.name.first().uppercaseChar()
                val previousFirstLetter = previousCity.name.first().uppercaseChar()
                
                if (currentFirstLetter != previousFirstLetter) {
                    letterHeader.visibility = View.VISIBLE
                    letterHeader.text = currentFirstLetter.toString()
                } else {
                    letterHeader.visibility = View.GONE
                }
            } else {
                // First item always shows header
                letterHeader.visibility = View.VISIBLE
                letterHeader.text = city.name.first().uppercaseChar().toString()
            }
            
            // Set index letter
            indexLetter.text = city.name.first().uppercaseChar().toString()
        }
    }
    
    class CityDiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem == newItem
        }
    }
} 