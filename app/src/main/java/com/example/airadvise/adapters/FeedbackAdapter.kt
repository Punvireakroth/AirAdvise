package com.example.airadvise.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airadvise.R
import com.example.airadvise.models.Feedback
import java.text.SimpleDateFormat
import java.util.Locale

class FeedbackAdapter(
    private val context: Context,
    private val feedbackList: List<Feedback>
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        
        holder.subjectTextView.text = feedback.subject
        holder.messageTextView.text = feedback.message
        
        // Format and set the date
        try {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                .parse(feedback.createdAt)
            holder.dateTextView.text = "Submitted: ${dateFormat.format(parsedDate!!)}"
        } catch (e: Exception) {
            holder.dateTextView.text = "Submitted: ${feedback.createdAt}"
        }
        
        // Set status and color
        holder.statusTextView.text = feedback.status.uppercase()
        when (feedback.status.lowercase()) {
            "pending" -> holder.statusTextView.background = 
                ContextCompat.getDrawable(context, R.drawable.bg_status_pending)
            "resolved" -> holder.statusTextView.background = 
                ContextCompat.getDrawable(context, R.drawable.bg_status_resolved)
        }
        
        // Handle response if available
        if (feedback.responses.isNotEmpty()) {
            val response = feedback.responses[0] // Get the first response
            holder.responseDivider.visibility = View.VISIBLE
            holder.responseContainer.visibility = View.VISIBLE
            holder.responseTextView.text = response.message
            
            try {
                val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                    .parse(response.createdAt)
                holder.responseDateTextView.text = "Responded: ${dateFormat.format(parsedDate!!)}"
            } catch (e: Exception) {
                holder.responseDateTextView.text = "Responded: ${response.createdAt}"
            }
        } else {
            holder.responseDivider.visibility = View.GONE
            holder.responseContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = feedbackList.size

    class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectTextView: TextView = itemView.findViewById(R.id.textViewSubject)
        val statusTextView: TextView = itemView.findViewById(R.id.textViewStatus)
        val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        val responseDivider: View = itemView.findViewById(R.id.responseDivider)
        val responseContainer: LinearLayout = itemView.findViewById(R.id.responseContainer)
        val responseTextView: TextView = itemView.findViewById(R.id.textViewResponse)
        val responseDateTextView: TextView = itemView.findViewById(R.id.textViewResponseDate)
    }
} 