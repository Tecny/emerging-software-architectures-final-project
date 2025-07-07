package Beans.tickets

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketNumber: String,
    val fullName: String,
    val transferType: String,
    val bankName: String,
    val accountNumber: String,
    val createdAt: String?,
    val status: String,
    val amount: Long
)
