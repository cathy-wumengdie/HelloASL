package ca.uwaterloo.helloasl.domain.userModel

data class UserSession(
    val userId: Int,
    val userName: String,
    val email: String,
    val loginTime: Long
)