package ca.uwaterloo.helloasl.domain.userModel

data class UserCredential (
    val userId: Int,
    val passwordHash: Int
)