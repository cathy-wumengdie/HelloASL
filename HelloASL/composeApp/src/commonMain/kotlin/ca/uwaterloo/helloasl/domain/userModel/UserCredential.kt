package ca.uwaterloo.helloasl.domain.userModel

data class UserCredential (
    val userId: String,
    val passwordHash: Int
)