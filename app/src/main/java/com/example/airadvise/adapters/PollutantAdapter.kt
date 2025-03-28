package com.example.airadvise.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.airadvise.databinding.ItemPollutantBinding
import com.example.airadvise.models.Pollutant
import com.example.airadvise.utils.AQIUtils

class PollutantAdapter(
    private val pollutants: List<Pollutant>,
    private val onItemClick: (Pollutant) -> Unit
) : RecyclerView.Adapter<PollutantAdapter.PollutantViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollutantViewHolder {
        val binding = ItemPollutantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PollutantViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PollutantViewHolder, position: Int) {
        holder.bind(pollutants[position])
    }
    
    override fun getItemCount() = pollutants.size
    
    inner class PollutantViewHolder(private val binding: ItemPollutantBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(pollutants[position])
                }
            }
        }
        
        fun bind(pollutant: Pollutant) {
            val context = binding.root.context
            
            binding.tvPollutantName.text = pollutant.name
            binding.tvPollutantValue.text = "${pollutant.value} ${pollutant.unit}"
            binding.tvPollutantDescription.text = pollutant.description
            
            // Set progress based on pollutant index (0-100)
            binding.progressBar.progress = pollutant.index
            
            // Set color based on index
            binding.progressBar.progressTintList = ColorStateList.valueOf(
                AQIUtils.getAQIColor(context, pollutant.index)
            )
        }
    }
} 