package ca.uwaterloo.helloasl.domain.starModel

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class StarRow(
    @SerialName("user_id")
    val userId: String,

    @SerialName("sign_id")
    val signId: Long,

    @SerialName("tag_id")
    val tagId: Long
)