package ca.uwaterloo.helloasl.domain.trackingModel

import kotlinx.datetime.*

// All the progress tracking classes should to up to date
data class DailyProgress(
    val minutesLearned: Int,
    val lastDailyGoalCompletedDate: LocalDate?,     // prevents double counting, null = never learned yet
    val dailyGoalMinutes: Int
) {
    val remainingMinutes: Int = (dailyGoalMinutes - minutesLearned).coerceAtLeast(0)
    val isDailyGoalMet: Boolean = dailyGoalMinutes > 0 && minutesLearned >= dailyGoalMinutes
}

data class WeeklyProgress(
    val daysCompleted: Int,
    val lastCreditedDate: LocalDate?,    // prevents double counting, null = never learned this week yet
    val weeklyGoalDays: Int
) {
    val remainingDays: Int = (weeklyGoalDays - daysCompleted).coerceAtLeast(0)
    val isWeeklyGoalMet: Boolean = daysCompleted >= weeklyGoalDays
}

fun updateDayStreak(
    currentStreak: Int,
    lastDailyGoalCompletedDate: LocalDate?, // from DailyProgress
    today: LocalDate,
    isDailyGoalCompleted: Boolean
): Pair<Int, LocalDate?> {
    // If daily goal not completed, no changes to streak or lastCompletedDate
    if (!isDailyGoalCompleted) return currentStreak to lastDailyGoalCompletedDate
    // If already recorded today, do nothing prevents double counting
    if (lastDailyGoalCompletedDate == today) return currentStreak to lastDailyGoalCompletedDate

    val yesterday = today.minus(DatePeriod(days = 1))
    val newStreak = when (lastDailyGoalCompletedDate) {
        null -> 1
        yesterday -> currentStreak + 1
        else -> 1
    }
    return newStreak to today
}

data class ProgressSummary(
    val userId: String,
    val date: LocalDate,    // progress summary should always be up to date. If date is not today then update needed.
    val dailyProgress: DailyProgress,
    val weeklyProgress: WeeklyProgress,
    val dayStreak: Int
)