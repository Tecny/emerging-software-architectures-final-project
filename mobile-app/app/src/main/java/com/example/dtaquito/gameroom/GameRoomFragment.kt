
package com.example.dtaquito.gameroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import Beans.rooms.GameRoom
import Interface.PlaceHolder
import network.RetrofitClient


class GameRoomFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var recycler: RecyclerView
    private var sportType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar Retrofit solo una vez por aplicación
        RetrofitClient.initialize(requireContext().applicationContext)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)
        sportType = arguments?.getString("SPORT_TYPE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_soccer_room, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        getAllRooms()
    }

    private fun getAllRooms() {
        service.getAllRooms().enqueue(object : retrofit2.Callback<List<GameRoom>> {
            override fun onResponse(
                call: retrofit2.Call<List<GameRoom>>,
                response: retrofit2.Response<List<GameRoom>>
            ) {
                if (!response.isSuccessful) {
                    requireContext().showToast("Error al obtener las salas")
                    return
                }
                val gameRooms = response.body()
                if (gameRooms.isNullOrEmpty()) {
                    requireContext().showToast("No se encontraron salas")
                    return
                }
                // Filtrar por tipo de deporte si corresponde
                val filteredRooms = sportType?.let { type ->
                    gameRooms.filter { it.reservation?.sportSpace?.sportType == type }
                } ?: gameRooms

                if (filteredRooms.isNotEmpty()) {
                    recycler.adapter = GameRoomAdapter(filteredRooms)
                } else {
                    requireContext().showToast("No se encontraron salas para $sportType")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<GameRoom>>, t: Throwable) {
                requireContext().showToast("Error de red")
            }
        })
    }
}