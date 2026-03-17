package ca.uwaterloo.helloasl.data.progressTrackerRepository

import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary

interface ProgressTrackerRepository {
    /** Returns an up-to-date ProgressSummary (auto refresh if date/week changed). */
    suspend fun getProgressSummary(): ProgressSummary

    /** Adds minutes to today’s learning minutes, and updates streak + weekly progress. Returns updated summary. */
    suspend fun addLearningMinutes(minutes: Int): ProgressSummary
}