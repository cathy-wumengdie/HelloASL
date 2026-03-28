package ca.uwaterloo.helloasl.data.notificationRepository

class MockNotificationRepository : NotificationRepository {
    override suspend fun triggerSendMissedReminder() {
        println("Mock: triggerSendMissedReminder called")
    }
}