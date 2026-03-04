package ca.uwaterloo.helloasl.data.progressTrackerRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary

class MockProgressTrackerRepository(private val db: MockDB): ProgressTrackerRepository {
    override fun getProgressSummary(): ProgressSummary = db.getProgressSummary()
    override fun addLearningMinutes(minutes: Int): ProgressSummary {
        db.addLearningMinutes(minutes)
        return db.getProgressSummary()
    }
}