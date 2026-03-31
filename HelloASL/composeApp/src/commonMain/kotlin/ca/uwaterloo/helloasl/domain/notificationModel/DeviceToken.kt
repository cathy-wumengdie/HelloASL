package ca.uwaterloo.helloasl.domain.notificationModel

import kotlinx.serialization.Serializable

@Serializable
data class DeviceToken(
    val user_id: String,
    val token: String,
    val platform: String,
    val device_id: String,
    val updated_at: String? = null
)