package ca.uwaterloo.helloasl.data.notificationRepository

interface NotificationRepository {
    suspend fun triggerSendMissedReminder()
}