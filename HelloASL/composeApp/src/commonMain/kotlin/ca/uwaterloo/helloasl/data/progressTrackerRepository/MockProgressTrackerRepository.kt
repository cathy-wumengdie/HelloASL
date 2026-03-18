package ca.uwaterloo.helloasl.data.progressTrackerRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameDate
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameWeek
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import kotlinx.datetime.LocalDate

class MockProgressTrackerRepository(
    private val db: MockDB
) : ProgressTrackerRepository {

    override suspend fun getProgressSummary(): ProgressSummary {
        return refreshProgressSummary()
    }

    override suspend fun addLearningMinutes(minutes: Int): ProgressSummary {
        require(minutes > 0) { "minutes must be > 0" }

        val userId = db.requireCurrentUserId()
        val t = today()
        val current = refreshProgressSummary()

        val newMinutes = current.dailyProgress.minutesLearned + minutes
        var newDailyProgress = current.dailyProgress.copy(minutesLearned = newMinutes)

        val (newStreak, newLastDailyGoalCompletedDate) = updateDayStreak(
            currentStreak = current.dayStreak,
            lastDailyGoalCompletedDate = current.dailyProgress.lastDailyGoalCompletedDate,
            today = t,
            isDailyGoalCompleted = newDailyProgress.isDailyGoalMet
        )

        newDailyProgress = newDailyProgress.copy(
            lastDailyGoalCompletedDate = newLastDailyGoalCompletedDate
        )

        var updated = current.copy(
            date = t,
            dailyProgress = newDailyProgress,
            dayStreak = newStreak
        )

        updated = updateWeeklyProgress(
            today = t,
            before = current,
            after = updated
        )

        db.putProgressSummary(userId, updated)
        return updated
    }

    override suspend fun reevaluateProgressAfterGoalChange(
        dailyGoalMinutes: Int,
        weeklyGoalDays: Int
    ): ProgressSummary {
        require(dailyGoalMinutes >= 0) { "dailyGoalMinutes must be >= 0" }
        require(weeklyGoalDays >= 0) { "weeklyGoalDays must be >= 0" }

        val userId = db.requireCurrentUserId()
        val t = today()
        val current = refreshProgressSummary()

        val updatedDaily = current.dailyProgress.copy(
            dailyGoalMinutes = dailyGoalMinutes
        )

        val (newStreak, newLastDailyGoalCompletedDate) = updateDayStreak(
            currentStreak = current.dayStreak,
            lastDailyGoalCompletedDate = current.dailyProgress.lastDailyGoalCompletedDate,
            today = t,
            isDailyGoalCompleted = updatedDaily.isDailyGoalMet
        )

        val creditedDaily = updatedDaily.copy(
            lastDailyGoalCompletedDate = newLastDailyGoalCompletedDate
        )

        var updated = current.copy(
            date = t,
            dailyProgress = creditedDaily,
            weeklyProgress = current.weeklyProgress.copy(
                weeklyGoalDays = weeklyGoalDays
            ),
            dayStreak = newStreak
        )

        updated = updateWeeklyProgress(
            today = t,
            before = current,
            after = updated
        )

        db.putProgressSummary(userId, updated)
        return updated
    }

    private fun refreshProgressSummary(): ProgressSummary {
        val userId = db.requireCurrentUserId()
        val t = today()
        val existing = db.getProgressSummary(userId)
            ?: error("Progress summary not found for user $userId")

        if (isSameDate(existing.date, t)) return existing

        var refreshed = existing.copy(
            date = t,
            dailyProgress = existing.dailyProgress.copy(minutesLearned = 0)
        )

        if (!isSameWeek(existing.date, t)) {
            refreshed = refreshed.copy(
                weeklyProgress = existing.weeklyProgress.copy(
                    daysCompleted = 0,
                    lastCreditedDate = null
                )
            )
        }

        db.putProgressSummary(userId, refreshed)
        return refreshed
    }

    private fun updateWeeklyProgress(
        today: LocalDate,
        before: ProgressSummary,
        after: ProgressSummary
    ): ProgressSummary {
        if (before.dailyProgress.isDailyGoalMet) return after
        if (!after.dailyProgress.isDailyGoalMet) return after
        if (after.weeklyProgress.lastCreditedDate == today) return after

        val newWeekly = after.weeklyProgress.copy(
            daysCompleted = after.weeklyProgress.daysCompleted + 1,
            lastCreditedDate = today
        )

        return after.copy(weeklyProgress = newWeekly)
    }
}