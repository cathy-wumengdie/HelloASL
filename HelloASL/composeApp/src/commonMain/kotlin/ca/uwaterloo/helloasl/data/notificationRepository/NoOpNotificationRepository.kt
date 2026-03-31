package ca.uwaterloo.helloasl.data.notificationRepository

object NoOpNotificationRepository : NotificationRepository {
    override suspend fun triggerSendMissedReminder() {
        println("NotificationRepository unavailable; skipping reminder trigger.")
    }
}