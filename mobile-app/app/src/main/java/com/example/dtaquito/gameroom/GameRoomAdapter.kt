package com.example.dtaquito.gameroom

import Beans.rooms.GameRoom
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class GameRoomAdapter(private val gameRoomsList: List<GameRoom>) : RecyclerView.Adapter<GameRoomsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameRoomsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GameRoomsViewHolder(layoutInflater.inflate(R.layout.card_room, parent, false))
    }

    override fun getItemCount(): Int = gameRoomsList.size

    override fun onBindViewHolder(holder: GameRoomsViewHolder, position: Int) {
        val item = gameRoomsList[position]
        holder.renderRoom(item)
    }
}