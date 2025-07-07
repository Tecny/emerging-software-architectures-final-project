package Beans.blockChain

data class BlockChain(
    val txHash: String? = null,
    val inputHex: String? = null,
    val timestamp: String? = null,
    val userId: Int? = null,
    val spaceId: Int? = null
)