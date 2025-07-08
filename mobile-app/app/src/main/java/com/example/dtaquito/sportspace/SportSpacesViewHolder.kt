package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.reservation.CreateReservationFragment
import com.example.dtaquito.utils.loadImageFromUrl

class SportSpacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val sportSpaceName: TextView = view.findViewById(R.id.txtTitle)
    private val sportSpaceImage: ImageView = view.findViewById(R.id.imgSportSpace)
    private val sportSpaceGameMode: TextView = view.findViewById(R.id.txtTypeBadge)
    private val sportSpacePrice: TextView = view.findViewById(R.id.txtPriceTag)
    private val txtAddress: TextView = view.findViewById(R.id.txtAddress)
    private val btnReservar: Button = view.findViewById(R.id.btnReservar)

    fun render(sportSpace: SportSpace) {
        val context = itemView.context
        val typeface = ResourcesCompat.getFont(context, R.font.righteous)

        sportSpaceName.typeface = typeface
        sportSpaceGameMode.typeface = typeface
        sportSpacePrice.typeface = typeface
        txtAddress.typeface = typeface

        sportSpaceName.text = sportSpace.name
        txtAddress.text = sportSpace.address
        sportSpaceGameMode.text = context.getString(
            R.string.game_mode_label,
            getLocalizedGameMode(context, sportSpace.gamemodeType)
        )
        sportSpacePrice.text = context.getString(
            R.string.price_label,
            sportSpace.price.toInt()
        )
        loadImageFromUrl(sportSpace.imageUrl, sportSpaceImage)

        // Mostrar/ocultar botón según el rol del usuario
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("role_type", null)

        if (role == "PLAYER") {
            btnReservar.visibility = View.VISIBLE
            btnReservar.setOnClickListener {
                val createReservationFragment = CreateReservationFragment()

                // Pasar el ID del espacio deportivo al fragmento de crear reserva
                val bundle = Bundle()
                bundle.putInt("sportSpaceId", sportSpace.id)
                createReservationFragment.arguments = bundle

                // Navegar al fragmento de crear reserva
                val activity = context as FragmentActivity
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, createReservationFragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            btnReservar.visibility = View.GONE
        }
    }
    private fun getLocalizedGameMode(context: Context, gamemodeType: String): String {
        return when (gamemodeType) {
            "FUTBOL_7" -> context.getString(R.string.soccer_7)
            "FUTBOL_8" -> context.getString(R.string.soccer_8)
            "FUTBOL_5" -> context.getString(R.string.soccer_5)
            "FUTBOL_11" -> context.getString(R.string.soccer_11)
            "POOL_3" -> context.getString(R.string.pool_3)
            // Añade más modos según necesites
            else -> gamemodeType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}