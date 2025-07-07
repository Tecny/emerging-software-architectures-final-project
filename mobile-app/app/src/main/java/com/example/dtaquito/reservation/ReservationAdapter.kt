package com.example.dtaquito.reservation

import Beans.reservations.Reservation
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.qr.QrFragment

class ReservationAdapter(
    private var reservations: List<Reservation> = emptyList(),
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ReservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.render(reservation)

        holder.qrBtn.setOnClickListener {
            reservation.id?.let { id ->
                val qrFragment = QrFragment.newInstance(id)
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, qrFragment)
                    .addToBackStack(null)
                    .commit()
            }
            Log.d("ReservationAdapter", "QR Button clicked for reservation ID: ${reservation.id}")
        }
    }

    override fun getItemCount(): Int = reservations.size

    fun updateReservations(newReservations: List<Reservation>) {
        reservations = newReservations
        notifyDataSetChanged()
    }
}