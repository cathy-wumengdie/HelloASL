package ca.uwaterloo.helloasl.domain.trackingModel

import kotlinx.datetime.*

data class DailyProgress(
    val minutesLearned: Int,
    val dailyGoalMinutes: Int
) {
    val remainingMinutes: Int = (dailyGoalMinutes - minutesLearned).coerceAtLeast(0)
    val isDailyGoalMet: Boolean = minutesLearned >= dailyGoalMinutes
}

data class WeeklyProgress(
    val daysCompleted: Int,
    val weeklyGoalDays: Int
) {
    val remainingDays: Int = (weeklyGoalDays - daysCompleted).coerceAtLeast(0)
    val isWeeklyGoalMet: Boolean = daysCompleted >= weeklyGoalDays
}

data class DayStreakState(
    val lastLearnedDate: LocalDate?, // null = never learned yet
    val currentStreak: Int
)

fun updateDayStreak(
    state: DayStreakState,
    today: LocalDate,
    isDailyGoalCompleted: Boolean // minutesLearned >= dailyGoalMinutes
): DayStreakState {
    // no changes if user has not completed the daily goal
    if (!isDailyGoalCompleted) return state

    // Already recorded today -> don't double count
    if (state.lastLearnedDate == today) return state

    // isDailyGoalCompleted == true && lastLearnedDate != today
    val yesterday = today.minus(DatePeriod(days = 1))
    val newStreak = when (state.lastLearnedDate) {
        null -> 1
        today.minus(DatePeriod(days = 1)) -> state.currentStreak + 1
        else -> 1
    }

    return DayStreakState(
        lastLearnedDate = today,
        currentStreak = newStreak
    )
}

data class ProgressSummary(
    val userId: Int,
    val date: LocalDate,    // progress summary should always be up to date. If date is not today then update needed.
    val dailyProgress: DailyProgress,
    val weeklyProgress: WeeklyProgress,
    val dayStreakState: DayStreakState
)