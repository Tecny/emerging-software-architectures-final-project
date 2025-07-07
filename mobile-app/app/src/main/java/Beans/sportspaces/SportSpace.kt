package Beans.sportspaces

import Beans.userProfile.UserProfile

data class SportSpace(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val address: String,
    val sportType: String,
    val gamemode: String,
    val price: Int,
    val amount: Int,
    // Campos adicionales para funcionalidad completa (opcionales)
    val sportId: Int? = null,
    val description: String? = null,
    val user: UserProfile? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val gamemodeId: Int? = null,
    val gamemodeType: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)