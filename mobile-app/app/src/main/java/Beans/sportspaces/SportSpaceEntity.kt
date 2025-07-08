package Beans.sportspaces

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sportspaces")
data class SportSpaceEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val gamemodeType: String,
    val price: Double,
    val image: String
)