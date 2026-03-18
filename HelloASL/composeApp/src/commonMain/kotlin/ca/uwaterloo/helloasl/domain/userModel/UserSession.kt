package ca.uwaterloo.helloasl.domain.userModel

data class UserSession(
    val userId: String,
    val userName: String,
    val email: String
)