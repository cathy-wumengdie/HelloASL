package ca.uwaterloo.helloasl.data.notificationRepository

interface DeviceTokenSyncer {
    suspend fun syncToken(token: String)
}