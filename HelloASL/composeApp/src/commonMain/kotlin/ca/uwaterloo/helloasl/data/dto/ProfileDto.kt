package ca.uwaterloo.helloasl.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val email: String
)
