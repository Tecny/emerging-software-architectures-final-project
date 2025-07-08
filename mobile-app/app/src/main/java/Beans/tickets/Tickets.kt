package Beans.tickets

data class Tickets(
    val fullName: String,
    val transferType: String,
    val bankName: String,
    val accountNumber: String,
    val createdAt: String?,
    val status: String,
    val ticketNumber: String,
    val amount: Long
)
data class CreateTicketRequest(
    val fullName: String,
    val transferType: String,
    val bankName: String,
    val accountNumber: String
)

