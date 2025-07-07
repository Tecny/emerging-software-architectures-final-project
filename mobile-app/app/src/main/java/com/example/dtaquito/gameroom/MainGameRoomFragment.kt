package com.example.dtaquito.gameroom

import Beans.playerList.Player
import Beans.rooms.GameRoom
import Beans.playerList.PlayerList
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.chat.ChatRoomFragment
import com.example.dtaquito.player.PlayerListAdapter
import com.example.dtaquito.utils.showToast
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainGameRoomFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var recyclerView: RecyclerView
    private lateinit var roomNameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var formatTextView: TextView
    private lateinit var leaveBtn: Button
    private lateinit var openChatButton: Button

    private var gameRoomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameRoomId = it.getInt("GAME_ROOM_ID", -1)
        }
        RetrofitClient.initialize(requireContext().applicationContext)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_main_game_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)

        if (gameRoomId != -1) {
            fetchGameRoomDetails(gameRoomId)
            fetchPlayerListByRoomId(gameRoomId)
        } else {
            requireContext().showToast("Invalid game room ID")
        }
        leaveBtn.setOnClickListener {
            if (gameRoomId != -1) {
                leaveRoom(gameRoomId)
            }
        }
        openChatButton.setOnClickListener {
            val fragment = ChatRoomFragment()
            val args = Bundle()
            args.putInt("GAME_ROOM_ID", gameRoomId)
            fragment.arguments = args
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initializeViews(view: View) {
        roomNameTextView = view.findViewById(R.id.roomName)
        addressTextView = view.findViewById(R.id.address)
        dateTextView = view.findViewById(R.id.date)
        timeTextView = view.findViewById(R.id.time)
        endTimeTextView = view.findViewById(R.id.endTime)
        formatTextView = view.findViewById(R.id.format)
        leaveBtn = view.findViewById(R.id.leaveButton)
        leaveBtn.visibility = View.VISIBLE
        openChatButton = view.findViewById(R.id.openChatButton)
        recyclerView = view.findViewById(R.id.playerList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchGameRoomDetails(gameRoomId: Int) {
        service.getRoomById(gameRoomId).enqueue(object : Callback<GameRoom> {
            override fun onResponse(call: Call<GameRoom>, response: Response<GameRoom>) {
                if (response.isSuccessful) {
                    response.body()?.let { gameRoom ->
                        val reservation = gameRoom.reservation
                        val sportSpace = reservation?.sportSpace

                        roomNameTextView.text = reservation?.reservationName ?: "Sin nombre"
                        addressTextView.text = "Dirección: ${sportSpace?.address ?: "Desconocida"}"
                        dateTextView.text = "Fecha: ${reservation?.gameDay ?: ""}"
                        timeTextView.text = "Hora de inicio: ${reservation?.startTime ?: ""}"
                        endTimeTextView.text = "Hora de fin: ${reservation?.endTime ?: ""}"
                        formatTextView.text = "Formato: ${sportSpace?.gamemode ?: "N/A"}"
                    } ?: run {
                        logAndShowError("No se encontró la sala")
                    }
                } else {
                    logAndShowError("Error al obtener la sala: HTTP ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GameRoom>, t: Throwable) {
                logAndShowError("Error: ${t.message}")
            }
        })
    }

    private fun fetchPlayerListByRoomId(roomId: Int) {
        service.getPlayerListByRoomId(roomId).enqueue(object : Callback<List<Player>> {
            override fun onResponse(call: Call<List<Player>>, response: Response<List<Player>>) {
                if (response.isSuccessful) {
                    response.body()?.let { player ->
                        recyclerView.adapter = PlayerListAdapter(player, service)
                    }
                } else {
                    requireContext().showToast("No se pudo obtener la lista de jugadores")
                }
            }

            override fun onFailure(call: Call<List<Player>>, t: Throwable) {
                requireContext().showToast("Error: ${t.message}")
            }
        })
    }

    private fun leaveRoom(roomId: Int) {
        service.leaveRoom(roomId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    requireContext().showToast("Has salido de la sala")
                    fetchPlayerListByRoomId(roomId)
                    leaveBtn.visibility = View.GONE
                } else {
                    requireContext().showToast("No se pudo salir de la sala")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                requireContext().showToast("Error: ${t.message}")
            }
        })
    }


    private fun logAndShowError(message: String) {
        Log.e("MainGameRoomFragment", message)
        requireContext().showToast(message)
    }
}