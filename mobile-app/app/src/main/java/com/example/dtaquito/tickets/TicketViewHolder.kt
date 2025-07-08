package com.example.dtaquito.tickets

import Beans.tickets.Tickets
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import java.text.SimpleDateFormat
import java.util.Locale

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
    private val bankNameTextView: TextView = itemView.findViewById(R.id.bankNameTextView)
    private val transferTypeTextView: TextView = itemView.findViewById(R.id.transferTypeTextView)
    private val accountNumberTextView: TextView = itemView.findViewById(R.id.accountNumberTextView)
    private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
    private val ticketNumberTextView: TextView = itemView.findViewById(R.id.ticketNumberTextView)
    private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    private val createdTextView: TextView = itemView.findViewById(R.id.createdTextView)

    fun bind(ticket: Tickets) {
        val context = itemView.context
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())

        val formattedDate = if (!ticket.createdAt.isNullOrEmpty()) {
            try {
                val createdDate = inputFormat.parse(ticket.createdAt)
                outputFormat.format(createdDate)
            } catch (e: Exception) {
                "Fecha inválida"
            }
        } else {
            "Sin fecha"
        }
        fullNameTextView.text = context.getString(R.string.full_name_label, ticket.fullName)
        bankNameTextView.text = context.getString(R.string.bank_name_label, ticket.bankName)
        transferTypeTextView.text = context.getString(R.string.transfer_type_label, ticket.transferType)
        accountNumberTextView.text = context.getString(R.string.account_number_label, ticket.accountNumber)
        statusTextView.text = context.getString(R.string.status_label, ticket.status.toString())
        ticketNumberTextView.text = context.getString(R.string.ticket_number, ticket.ticketNumber)
        amountTextView.text = context.getString(R.string.amount_label, ticket.amount)
        createdTextView.text = context.getString(R.string.created_label, formattedDate)
    }
}