package Beans.auth.login

data class LoginResponse(
    val id: Int,
    val username: String,
    val token: String,
)