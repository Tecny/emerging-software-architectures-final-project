package com.example.dtaquito.player

import Beans.playerList.Player
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class PlayerListAdapter(
    private val playerList: List<Player>,
    private val service: Interface.PlaceHolder // Puedes eliminar este parámetro si ya no lo usas
) : RecyclerView.Adapter<PlayerListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_list_item, parent, false)
        return PlayerListViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerListViewHolder, position: Int) {
        val player = playerList[position]
        holder.renderPlayer(player.name)
    }

    override fun getItemCount(): Int = playerList.size
}