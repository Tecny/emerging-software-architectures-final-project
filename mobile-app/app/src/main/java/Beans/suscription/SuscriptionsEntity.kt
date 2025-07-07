package Beans.suscription

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SuscriptionsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val price: String,
    val details: String,
    val backgroundColor: Int,
    val planType: String
)