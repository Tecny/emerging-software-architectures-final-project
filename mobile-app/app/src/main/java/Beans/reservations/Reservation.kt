package Beans.reservations

import Beans.sportspaces.SportSpace

data class Reservation(
    val id: Int? = null,
    val name: String? = null,
    val reservationName: String? = null,
    val gameDay: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val userId: Int? = null,
    val userName: String? = null,
    val sportSpacesId: Int,
    val sportSpaces: SportSpace? = null,
    val sportSpace: SportSpace? = null,
    val type: String? = null,
    val status: String? = null
)