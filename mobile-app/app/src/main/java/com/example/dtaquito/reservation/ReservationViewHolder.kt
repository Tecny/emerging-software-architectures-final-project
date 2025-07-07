package com.example.dtaquito.reservation

import Beans.reservations.Reservation
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val txtName: TextView = itemView.findViewById(R.id.txtName)
    val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
    val txtGameMode: TextView = itemView.findViewById(R.id.txtGameMode)
    val txtDate: TextView = itemView.findViewById(R.id.txtDate)
    val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    val txtSportSpaceName: TextView = itemView.findViewById(R.id.txtSportSpaceName)
    val txtPlace: TextView = itemView.findViewById(R.id.txtPlace)
    val qrBtn: ImageButton = itemView.findViewById(R.id.qr_btn)

    fun render(reservation: Reservation) {



        // Título de la reservación
        txtName.text = reservation.name ?: itemView.context.getString(R.string.no_name)

        // Estado de la reservación con badge
        val status = reservation.status ?: "Unknown"
        txtStatus.text = when(status.uppercase()) {
            "CONFIRMED" -> itemView.context.getString(R.string.confirmed)
            "PENDING" -> itemView.context.getString(R.string.pending)
            "CANCELLED" -> itemView.context.getString(R.string.cancelled)
            else -> status
        }

        // Game mode con formato "Game mode: Football 7"
        val sportSpace = reservation.sportSpaces
        val gameMode = when(sportSpace?.gamemode) {
            "FUTBOL_5" -> itemView.context.getString(R.string.soccer_5)
            "FUTBOL_7" -> itemView.context.getString(R.string.soccer_7)
            "FUTBOL_8" -> itemView.context.getString(R.string.soccer_8)
            "FUTBOL_11" -> itemView.context.getString(R.string.soccer_11)
            "BILLAR_3" -> itemView.context.getString(R.string.pool_3)
            else -> sportSpace?.gamemode ?: itemView.context.getString(R.string.not_specified)
        }
        txtGameMode.text = itemView.context.getString(
            R.string.game_mode_format,
            itemView.context.getString(R.string.game_mode_room),
            gameMode
        )

        // Date combinando fecha y horarios "Date: Sun 06/07, 21:00 - 22:00"
        val gameDay = reservation.gameDay ?: ""
        val startTime = reservation.startTime ?: ""
        val endTime = reservation.endTime ?: ""

        // Formatear fecha a formato más legible
        val formattedDate = try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("EEE dd/MM", java.util.Locale.getDefault())
            val date = inputFormat.parse(gameDay)
            val dateString = date?.let { outputFormat.format(it) } ?: gameDay
            // Capitalizar la primera letra del día de la semana
            dateString.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } catch (e: Exception) {
            gameDay
        }

        txtDate.text = itemView.context.getString(
            R.string.date_format,
            itemView.context.getString(R.string.date_label),
            formattedDate,
            startTime,
            endTime
        )

        // Reservation price con formato "Reservation price: 50 créditos"
        txtPrice.text = itemView.context.getString(
            R.string.reservation_price_format,
            itemView.context.getString(R.string.reservation_price_label),
            sportSpace?.price ?: 0,
            itemView.context.getString(R.string.credits_room)
        )

        txtSportSpaceName.text = itemView.context.getString(
            R.string.sport_space_format,
            itemView.context.getString(R.string.sport_space_label),
            sportSpace?.name ?: itemView.context.getString(R.string.no_space)
        )

        // Place con formato "Place: [dirección completa]"
        txtPlace.text = itemView.context.getString(
            R.string.place_format,
            itemView.context.getString(R.string.place_label),
            sportSpace?.address ?: itemView.context.getString(R.string.no_address)
        )
    }
}