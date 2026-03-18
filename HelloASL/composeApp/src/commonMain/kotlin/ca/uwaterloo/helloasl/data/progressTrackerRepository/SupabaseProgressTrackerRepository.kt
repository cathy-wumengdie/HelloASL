package ca.uwaterloo.helloasl.data.progressTrackerRepository

import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.updateDayStreak
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today

class SupabaseProgressTrackerRepository(
    private val supabase: SupabaseClient
) : ProgressTrackerRepository {

    @Serializable
    private data class ProgressSummaryRow(
        @SerialName("user_id") val userId: String,
        @SerialName("last_update_date") val lastUpdateDate: String,
        @SerialName("daily_minutes_learned") val dailyMinutesLearned: Int,
        @SerialName("last_daily_goal_completed_date") val lastDailyGoalCompletedDate: String? = null,
        @SerialName("daily_goal_minutes") val dailyGoalMinutes: Int,
        @SerialName("weekly_days_completed") val weeklyDaysCompleted: Int,
        @SerialName("last_weekly_credited_date") val lastWeeklyCreditedDate: String? = null,
        @SerialName("weekly_goal_days") val weeklyGoalDays: Int,
        @SerialName("day_streak") val dayStreak: Int
    )

    @Serializable
    private data class ProgressSummaryInsertRow(
        @SerialName("user_id") val userId: String,
        @SerialName("last_update_date") val lastUpdateDate: String,
        @SerialName("daily_minutes_learned") val dailyMinutesLearned: Int,
        @SerialName("last_daily_goal_completed_date") val lastDailyGoalCompletedDate: String? = null,
        @SerialName("daily_goal_minutes") val dailyGoalMinutes: Int,
        @SerialName("weekly_days_completed") val weeklyDaysCompleted: Int,
        @SerialName("last_weekly_credited_date") val lastWeeklyCreditedDate: String? = null,
        @SerialName("weekly_goal_days") val weeklyGoalDays: Int,
        @SerialName("day_streak") val dayStreak: Int
    )

    @Serializable
    private data class ProgressSummaryUpdateRow(
        @SerialName("last_update_date") val lastUpdateDate: String,
        @SerialName("daily_minutes_learned") val dailyMinutesLearned: Int,
        @SerialName("last_daily_goal_completed_date") val lastDailyGoalCompletedDate: String? = null,
        @SerialName("daily_goal_minutes") val dailyGoalMinutes: Int,
        @SerialName("weekly_days_completed") val weeklyDaysCompleted: Int,
        @SerialName("last_weekly_credited_date") val lastWeeklyCreditedDate: String? = null,
        @SerialName("weekly_goal_days") val weeklyGoalDays: Int,
        @SerialName("day_streak") val dayStreak: Int
    )

    override suspend fun getProgressSummary(): ProgressSummary {
        val userId = requireUserId()
        val today = today()

        val current = getOrCreateRow(userId, today)
        val refreshed = refreshForDateOrWeek(current, today)

        return toDomain(refreshed)
    }

    override suspend fun addLearningMinutes(minutes: Int): ProgressSummary {
        require(minutes >= 0) { "minutes must be >= 0" }

        val userId = requireUserId()
        val today = today()

        val current = getOrCreateRow(userId, today)
        val base = refreshForDateOrWeek(current, today)

        val newDailyMinutes = base.dailyMinutesLearned + minutes

        val dailyGoalCompletedNow =
            base.dailyGoalMinutes > 0 && newDailyMinutes >= base.dailyGoalMinutes

        val (newStreak, newLastDailyGoalCompletedDate) = updateDayStreak(
            currentStreak = base.dayStreak,
            lastDailyGoalCompletedDate = base.lastDailyGoalCompletedDate?.let(::parseDate),
            today = today,
            isDailyGoalCompleted = dailyGoalCompletedNow
        )

        val alreadyCreditedThisWeekToday =
            base.lastWeeklyCreditedDate == today.toString()

        val shouldCreditWeeklyDay =
            dailyGoalCompletedNow && !alreadyCreditedThisWeekToday

        val newWeeklyDaysCompleted =
            if (shouldCreditWeeklyDay) base.weeklyDaysCompleted + 1 else base.weeklyDaysCompleted

        val newLastWeeklyCreditedDate =
            if (shouldCreditWeeklyDay) today.toString() else base.lastWeeklyCreditedDate

        val updated = ProgressSummaryUpdateRow(
            lastUpdateDate = today.toString(),
            dailyMinutesLearned = newDailyMinutes,
            lastDailyGoalCompletedDate = newLastDailyGoalCompletedDate?.toString(),
            dailyGoalMinutes = base.dailyGoalMinutes,
            weeklyDaysCompleted = newWeeklyDaysCompleted,
            lastWeeklyCreditedDate = newLastWeeklyCreditedDate,
            weeklyGoalDays = base.weeklyGoalDays,
            dayStreak = newStreak
        )

        supabase.from("ProgressSummary").update(updated) {
            filter { eq("user_id", userId) }
        }

        val finalRow = supabase
            .from("ProgressSummary")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ProgressSummaryRow>()
            .firstOrNull()
            ?: error("Updated ProgressSummary not found for user $userId")

        return toDomain(finalRow)
    }

    override suspend fun reevaluateProgressAfterGoalChange(
        dailyGoalMinutes: Int,
        weeklyGoalDays: Int
    ): ProgressSummary {
        require(dailyGoalMinutes >= 0) { "dailyGoalMinutes must be >= 0" }
        require(weeklyGoalDays >= 0) { "weeklyGoalDays must be >= 0" }

        val userId = requireUserId()
        val today = today()

        val current = getOrCreateRow(userId, today)
        val base = refreshForDateOrWeek(current, today)

        val dailyGoalCompletedNow =
            dailyGoalMinutes > 0 && base.dailyMinutesLearned >= dailyGoalMinutes

        val (newStreak, newLastDailyGoalCompletedDate) = updateDayStreak(
            currentStreak = base.dayStreak,
            lastDailyGoalCompletedDate = base.lastDailyGoalCompletedDate?.let(::parseDate),
            today = today,
            isDailyGoalCompleted = dailyGoalCompletedNow
        )

        val alreadyCreditedThisWeekToday =
            base.lastWeeklyCreditedDate == today.toString()

        val shouldCreditWeeklyDay =
            dailyGoalCompletedNow && !alreadyCreditedThisWeekToday

        val newWeeklyDaysCompleted =
            if (shouldCreditWeeklyDay) base.weeklyDaysCompleted + 1 else base.weeklyDaysCompleted

        val newLastWeeklyCreditedDate =
            if (shouldCreditWeeklyDay) today.toString() else base.lastWeeklyCreditedDate

        val updated = ProgressSummaryUpdateRow(
            lastUpdateDate = today.toString(),
            dailyMinutesLearned = base.dailyMinutesLearned,
            lastDailyGoalCompletedDate = newLastDailyGoalCompletedDate?.toString(),
            dailyGoalMinutes = dailyGoalMinutes,
            weeklyDaysCompleted = newWeeklyDaysCompleted,
            lastWeeklyCreditedDate = newLastWeeklyCreditedDate,
            weeklyGoalDays = weeklyGoalDays,
            dayStreak = newStreak
        )

        supabase.from("ProgressSummary").update(updated) {
            filter { eq("user_id", userId) }
        }

        val finalRow = supabase
            .from("ProgressSummary")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ProgressSummaryRow>()
            .firstOrNull()
            ?: error("Updated ProgressSummary not found for user $userId")

        return toDomain(finalRow)
    }

    private fun requireUserId(): String {
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No logged-in user")

        return session.user?.id
            ?: error("Logged-in user has no id")
    }

    private suspend fun getOrCreateRow(userId: String, today: LocalDate): ProgressSummaryRow {
        val existing = supabase
            .from("ProgressSummary")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ProgressSummaryRow>()
            .firstOrNull()

        if (existing != null) return existing

        val insertRow = ProgressSummaryInsertRow(
            userId = userId,
            lastUpdateDate = today.toString(),
            dailyMinutesLearned = 0,
            lastDailyGoalCompletedDate = null,
            dailyGoalMinutes = 0,
            weeklyDaysCompleted = 0,
            lastWeeklyCreditedDate = null,
            weeklyGoalDays = 0,
            dayStreak = 0
        )

        supabase.from("ProgressSummary").insert(insertRow)

        return supabase
            .from("ProgressSummary")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ProgressSummaryRow>()
            .firstOrNull()
            ?: error("Failed to create ProgressSummary for user $userId")
    }

    private suspend fun refreshForDateOrWeek(
        row: ProgressSummaryRow,
        today: LocalDate
    ): ProgressSummaryRow {
        val rowDate = parseDate(row.lastUpdateDate)
        val sameDay = rowDate == today
        val sameWeek = isSameWeek(rowDate, today)

        if (sameDay && sameWeek) return row

        val refreshed = ProgressSummaryUpdateRow(
            lastUpdateDate = today.toString(),
            dailyMinutesLearned = if (sameDay) row.dailyMinutesLearned else 0,
            lastDailyGoalCompletedDate = row.lastDailyGoalCompletedDate,
            dailyGoalMinutes = row.dailyGoalMinutes,
            weeklyDaysCompleted = if (sameWeek) row.weeklyDaysCompleted else 0,
            lastWeeklyCreditedDate = if (sameWeek) row.lastWeeklyCreditedDate else null,
            weeklyGoalDays = row.weeklyGoalDays,
            dayStreak = row.dayStreak
        )

        supabase.from("ProgressSummary").update(refreshed) {
            filter { eq("user_id", row.userId) }
        }

        return supabase
            .from("ProgressSummary")
            .select {
                filter { eq("user_id", row.userId) }
            }
            .decodeList<ProgressSummaryRow>()
            .firstOrNull()
            ?: error("Failed to refresh ProgressSummary for user ${row.userId}")
    }

    private fun toDomain(row: ProgressSummaryRow): ProgressSummary {
        return ProgressSummary(
            userId = row.userId,
            date = parseDate(row.lastUpdateDate),
            dailyProgress = DailyProgress(
                minutesLearned = row.dailyMinutesLearned,
                lastDailyGoalCompletedDate = row.lastDailyGoalCompletedDate?.let(::parseDate),
                dailyGoalMinutes = row.dailyGoalMinutes
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = row.weeklyDaysCompleted,
                lastCreditedDate = row.lastWeeklyCreditedDate?.let(::parseDate),
                weeklyGoalDays = row.weeklyGoalDays
            ),
            dayStreak = row.dayStreak
        )
    }

    private fun parseDate(value: String): LocalDate {
        return LocalDate.parse(value)
    }

    private fun isSameWeek(a: LocalDate, b: LocalDate): Boolean {
        return startOfWeek(a) == startOfWeek(b)
    }

    private fun startOfWeek(date: LocalDate): LocalDate {
        val daysFromMonday = date.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
        return date.minus(DatePeriod(days = daysFromMonday))
    }
}