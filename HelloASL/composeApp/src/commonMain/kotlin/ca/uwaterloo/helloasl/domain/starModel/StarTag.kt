package ca.uwaterloo.helloasl.domain.starModel

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class StarTag(
    @SerialName("tag_id")
    val id: Long,
    val name: String,

    @SerialName("user_id")
    val userId: String
)