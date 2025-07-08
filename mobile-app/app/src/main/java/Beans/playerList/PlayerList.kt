package Beans.playerList

data class PlayerList(
    val id: Int,
    val chatRoomId: Int,
    val roomId: Int,
    val userId: Int,
    val isRoomCreator: Boolean,
    val isMember: Boolean
)