package Beans.userProfile

data class UserProfile(
    val id: Int? = null,
    val name: String,
    val email: String,
    val password: String,
    val roleType: String,
    val credits: Double = 0.0,
)