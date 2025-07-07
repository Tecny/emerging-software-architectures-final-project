package com.example.dtaquito.gameroom

import Beans.playerList.PlayerList
import Beans.rooms.GameRoom
import Interface.PlaceHolder
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameRoomsViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val roomName: TextView = view.findViewById(R.id.txtName)
    private val roomPlayers: TextView = view.findViewById(R.id.txtPlayers)
    private val roomGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val roomDate: TextView = view.findViewById(R.id.txtDate)
    private val roomAmount: TextView = view.findViewById(R.id.txtAmount)
    private val roomSportSpaceName: TextView = view.findViewById(R.id.txtSportSpaceName)
    private val roomDistrict: TextView = view.findViewById(R.id.txtDistrict)
    private val joinButton: Button = itemView.findViewById(R.id.btnGoToRoom)
    private val deleteButton: Button = itemView.findViewById(R.id.btnDelete)

    private lateinit var currentGameRoom: GameRoom
    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)


    init {
        joinButton.setOnClickListener {
            val context = itemView.context
            val activity = context as? FragmentActivity
            if (activity != null) {
                checkUserRoomStatusAndJoinOrNavigate(currentGameRoom.id, activity)
            }
        }
    }

    private fun checkUserRoomStatusAndJoinOrNavigate(roomId: Int, activity: FragmentActivity) {
        service.getUserRoomStatus(roomId).enqueue(object : Callback<PlayerList> {
            override fun onResponse(call: Call<PlayerList>, response: Response<PlayerList>) {
                if (response.isSuccessful && response.body() != null) {
                    val playerStatus = response.body()!!
                    if (playerStatus.isRoomCreator || playerStatus.isMember) {
                        navigateToRoom(roomId, activity)
                    } else {
                        joinRoom(roomId, activity)
                    }
                } else {
                    joinRoom(roomId, activity)
                }
            }

            override fun onFailure(call: Call<PlayerList>, t: Throwable) {
                itemView.context.showToast("${itemView.context.getString(R.string.error)} ${t.message}")
            }
        })
    }

    private fun joinRoom(roomId: Int, activity: FragmentActivity) {
        service.joinRoom(roomId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    itemView.context.showToast(itemView.context.getString(R.string.joined_room))
                    navigateToRoom(roomId, activity)
                } else {
                    itemView.context.showToast(itemView.context.getString(R.string.could_not_join))
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                itemView.context.showToast("${itemView.context.getString(R.string.error)} ${t.message}")
            }
        })
    }

    private fun navigateToRoom(roomId: Int, activity: FragmentActivity) {
        val fragment = MainGameRoomFragment()
        val args = android.os.Bundle()
        args.putInt("GAME_ROOM_ID", roomId)
        fragment.arguments = args
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun renderRoom(gameRoom: GameRoom) {
        currentGameRoom = gameRoom
        val typeface = ResourcesCompat.getFont(itemView.context, R.font.righteous)
        val reservation = gameRoom.reservation
        val sportSpace = reservation?.sportSpace

        // Obtener el userId del usuario logueado desde SharedPreferences
        val prefs = itemView.context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("user_id", -1)

        // Mostrar/ocultar botón de eliminar según si el usuario es el creador
        val isCreator = reservation?.userId == currentUserId && currentUserId != -1
        deleteButton.visibility = if (isCreator) View.VISIBLE else View.GONE

        // Aplicar typeface a todos los elementos
        roomName.typeface = typeface
        roomPlayers.typeface = typeface
        roomGameMode.typeface = typeface
        roomDate.typeface = typeface
        roomAmount.typeface = typeface
        roomSportSpaceName.typeface = typeface
        roomDistrict.typeface = typeface

        // Mapear datos según la estructura del HTML y respuesta del backend
        roomName.text = reservation?.reservationName ?: itemView.context.getString(R.string.no_name)

        // Formato de jugadores: ya viene como "3/14" del backend
        roomPlayers.text = gameRoom.playerCount ?: "0/0"

        // Game mode con formato usando placeholder
        val gameMode = when(sportSpace?.gamemode) {
            "FUTBOL_5" -> itemView.context.getString(R.string.soccer_5)
            "FUTBOL_7" -> itemView.context.getString(R.string.soccer_7)
            "FUTBOL_8" -> itemView.context.getString(R.string.soccer_8)
            "FUTBOL_11" -> itemView.context.getString(R.string.soccer_11)
            "BILLAR_3" -> itemView.context.getString(R.string.pool_3)
            else -> sportSpace?.gamemode ?: itemView.context.getString(R.string.not_specified)
        }
        roomGameMode.text = itemView.context.getString(
            R.string.game_mode_format,
            itemView.context.getString(R.string.game_mode_room),
            gameMode
        )

        // Date combinando fecha y horarios "Date: Sun 06/07, 22:00 - 23:00"
        val gameDay = reservation?.gameDay ?: ""
        val startTime = reservation?.startTime ?: ""
        val endTime = reservation?.endTime ?: ""

        // Formatear fecha a formato más legible (opcional)
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

        roomDate.text = itemView.context.getString(
            R.string.date_format,
            itemView.context.getString(R.string.date_label),
            formattedDate,
            startTime,
            endTime
        )

        // Advance con formato usando placeholder
        roomAmount.text = itemView.context.getString(
            R.string.advance_format,
            itemView.context.getString(R.string.advance_label),
            sportSpace?.amount ?: 0,
            itemView.context.getString(R.string.credits_room)
        )

        // Sport space name con formato usando placeholder
        roomSportSpaceName.text = itemView.context.getString(
            R.string.sport_space_format,
            itemView.context.getString(R.string.sport_space_label),
            sportSpace?.name ?: itemView.context.getString(R.string.no_space)
        )

        // Place con formato usando placeholder
        roomDistrict.text = itemView.context.getString(
            R.string.place_format,
            itemView.context.getString(R.string.place_label),
            sportSpace?.address ?: itemView.context.getString(R.string.no_address)
        )
    }
}