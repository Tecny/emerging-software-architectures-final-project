package Beans.rooms

import Beans.reservations.Reservation

data class GameRoom(
    val id: Int,
    val reservation: Reservation?,
    val playerCount: String? = null,

)