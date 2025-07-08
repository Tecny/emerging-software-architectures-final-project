package com.example.dtaquito.chat

import Beans.chat.ChatMessage
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import java.text.SimpleDateFormat
import java.util.Locale

class  ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
    private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

    fun bind(message: ChatMessage) {
        val userName = message.userName.uppercase()
        userNameTextView.text = userName
        messageTextView.text = message.content
        timestampTextView.text = formatDate(message.createdAt)
    }
    fun formatDate(dateString: String): String {
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return try {
            val inputFormat1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val date = inputFormat1.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            try {
                val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat2.parse(dateString)
                outputFormat.format(date)
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
