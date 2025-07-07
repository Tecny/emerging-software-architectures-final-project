package Beans.chat

data class ChatMessage(
    val content: String,
    val userId: Int,
    val userName: String,
    val createdAt: String,
    val roomId: Int
)