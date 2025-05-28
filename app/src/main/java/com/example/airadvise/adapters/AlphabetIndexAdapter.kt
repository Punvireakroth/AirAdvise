package com.example.airadvise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airadvise.R

class AlphabetIndexAdapter(
    private val letters: List<Char>,
    private val onLetterSelected: (Char) -> Unit
) : RecyclerView.Adapter<AlphabetIndexAdapter.LetterViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alphabet_letter, parent, false)
        return LetterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        holder.bind(letters[position])
    }
    
    override fun getItemCount(): Int = letters.size
    
    inner class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val letterText: TextView = itemView.findViewById(R.id.letterText)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLetterSelected(letters[position])
                }
            }
        }
        
        fun bind(letter: Char) {
            letterText.text = letter.toString()
        }
    }
} 