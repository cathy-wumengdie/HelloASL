package ca.uwaterloo.helloasl.data.userRepository

import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserLearningProgress
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseUserRepository(
    private val supabase: SupabaseClient
) : UserRepository {

    @Serializable
    private data class ProfileRow(
        @SerialName("user_id") val userId: String,
        val name: String,
        val email: String
    )

    @Serializable
    private data class LessonRefRow(
        @SerialName("lesson_id") val lessonId: Long,
        @SerialName("module_id") val moduleId: Long
    )

    @Serializable
    private data class UserLearningProgressRow(
        @SerialName("user_id") val userId: String,
        @SerialName("lesson_id") val lessonId: Long? = null,
        @SerialName("completed_all_lessons") val completedAllLessons: Boolean,
        @SerialName("words_learned") val wordsLearned: Int = 0,
        @SerialName("Lesson") val lesson: LessonRefRow? = null
    )

    @Serializable
    private data class UserLearningProgressUpsertRow(
        @SerialName("user_id") val userId: String,
        @SerialName("lesson_id") val lessonId: Long?,
        @SerialName("completed_all_lessons") val completedAllLessons: Boolean,
        @SerialName("words_learned") val wordsLearned: Int = 0
    )

    @Serializable
    private data class UserLearningProgressUpdateRow(
        @SerialName("lesson_id") val lessonId: Long?,
        @SerialName("completed_all_lessons") val completedAllLessons: Boolean
    )

    @Serializable
    private data class UserLearningProgressUpdateWordsRow(
        @SerialName("words_learned") val wordsLearned: Int
    )

    @Serializable
    private data class ProgressSummaryRow(
        @SerialName("user_id") val userId: String,
        @SerialName("last_update_date") val lastUpdateDate: String,
        @SerialName("daily_minutes_learned") val dailyMinutesLearned: Int,
        @SerialName("daily_goal_minutes") val dailyGoalMinutes: Int,
        @SerialName("weekly_days_completed") val weeklyDaysCompleted: Int,
        @SerialName("weekly_goal_days") val weeklyGoalDays: Int,
        @SerialName("day_streak") val dayStreak: Int
    )

    @Serializable
    private data class ProgressSummaryGoalsRow(
        @SerialName("user_id") val userId: String,
        @SerialName("daily_goal_minutes") val dailyGoalMinutes: Int,
        @SerialName("weekly_goal_days") val weeklyGoalDays: Int
    )

    @Serializable
    private data class ProgressSummaryGoalsUpdateRow(
        @SerialName("daily_goal_minutes") val dailyGoalMinutes: Int,
        @SerialName("weekly_goal_days") val weeklyGoalDays: Int
    )

    @Serializable
    private data class SignIdRow(
        @SerialName("sign_id") val signId: Long
    )

    @Serializable
    private data class CompletedLessonRow(
        @SerialName("user_id") val userId: String,
        @SerialName("lesson_id") val lessonId: Long
    )

    private val userLearningColumns = Columns.raw(
        """
        user_id,
        lesson_id,
        completed_all_lessons,
        words_learned,
        Lesson (
          lesson_id,
          module_id
        )
        """.trimIndent()
    )

    private val lessonColumns = Columns.list("lesson_id", "module_id")
    private val signColumns = Columns.list("sign_id")

    override suspend fun getUser(): User {
        val userId = requireUserId()

        val profile = supabase
            .from("Profiles")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ProfileRow>()
            .firstOrNull()
            ?: error("Profile not found for user $userId")

        return User(
            id = profile.userId,
            name = profile.name,
            email = profile.email
        )
    }

    override suspend fun getUserLearningProgress(): UserLearningProgress {
        val userId = requireUserId()
        val row = getOrInitializeUserLearningProgressRow(userId)

        if (row.completedAllLessons) {
            return UserLearningProgress(
                userId = row.userId,
                moduleId = null,
                lessonId = null,
                completedAllLessons = true,
                wordsLearned = row.wordsLearned
            )
        }

        val lesson = row.lesson
            ?: error("Lesson join failed for user $userId")

        return UserLearningProgress(
            userId = row.userId,
            moduleId = lesson.moduleId,
            lessonId = lesson.lessonId,
            completedAllLessons = false,
            wordsLearned = row.wordsLearned
        )
    }

    override suspend fun updateLearningProgress(): Boolean {
        val userId = requireUserId()
        val currentRow = getOrInitializeUserLearningProgressRow(userId)

        if (currentRow.completedAllLessons) {
            return false
        }

        val currentLessonId = currentRow.lessonId
            ?: error("Current lesson_id is null while completed_all_lessons is false")

        val nextLesson = getNextLesson(currentLessonId)

        if (nextLesson == null) {
            supabase.from("UserLearningProgress").update(
                UserLearningProgressUpdateRow(
                    lessonId = null,
                    completedAllLessons = true
                )
            ) {
                filter { eq("user_id", userId) }
            }
            return true
        }

        supabase.from("UserLearningProgress").update(
            UserLearningProgressUpdateRow(
                lessonId = nextLesson.lessonId,
                completedAllLessons = false
            )
        ) {
            filter { eq("user_id", userId) }
        }

        return true
    }

    override suspend fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        val userId = requireUserId()

        val existing = supabase
            .from("ProgressSummary")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ProgressSummaryGoalsRow>()
            .firstOrNull()

        if (existing == null) {
            ensureProgressSummaryExists(
                userId = userId,
                dailyGoalMinutes = minutesPerDay,
                weeklyGoalDays = daysPerWeek
            )
            return
        }

        supabase.from("ProgressSummary").update(
            ProgressSummaryGoalsUpdateRow(
                dailyGoalMinutes = minutesPerDay,
                weeklyGoalDays = daysPerWeek
            )
        ) {
            filter { eq("user_id", userId) }
        }
    }

    override suspend fun completeLesson(lessonId: Long): Boolean {
        val userId = requireUserId()
        val currentRow = getOrInitializeUserLearningProgressRow(userId)

        val alreadyCompleted = supabase
            .from("CompletedLesson")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("lesson_id", lessonId)
                }
            }
            .decodeList<CompletedLessonRow>()
            .isNotEmpty()

        if (alreadyCompleted) return false

        supabase.from("CompletedLesson").insert(
            CompletedLessonRow(
                userId = userId,
                lessonId = lessonId
            )
        )

        val lessonWordCount = getLessonWordCount(lessonId)
        val newTotal = currentRow.wordsLearned + lessonWordCount

        supabase.from("UserLearningProgress").update(
            UserLearningProgressUpdateWordsRow(newTotal)
        ) {
            filter { eq("user_id", userId) }
        }

        return true
    }

    private suspend fun getLessonWordCount(lessonId: Long): Int {
        return supabase
            .from("ASLSign")
            .select(columns = signColumns) {
                filter { eq("lesson_id", lessonId) }
            }
            .decodeList<SignIdRow>()
            .size
    }

    private fun requireUserId(): String {
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No logged-in user")

        return session.user?.id
            ?: error("Logged-in user has no id")
    }

    private suspend fun getUserLearningProgressRow(userId: String): UserLearningProgressRow? {
        return supabase
            .from("UserLearningProgress")
            .select(columns = userLearningColumns) {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserLearningProgressRow>()
            .firstOrNull()
    }

    private suspend fun getOrInitializeUserLearningProgressRow(userId: String): UserLearningProgressRow {
        return getUserLearningProgressRow(userId) ?: initializeUserLearningProgress(userId)
    }

    private suspend fun initializeUserLearningProgress(userId: String): UserLearningProgressRow {
        val firstLesson = getFirstLesson()
            ?: error("No lessons exist, cannot initialize UserLearningProgress")

        val initRow = UserLearningProgressUpsertRow(
            userId = userId,
            lessonId = firstLesson.lessonId,
            completedAllLessons = false,
            wordsLearned = 0
        )

        try {
            supabase.from("UserLearningProgress").upsert(initRow)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize learning progress: ${e.message}", e)
        }

        return UserLearningProgressRow(
            userId = userId,
            lessonId = firstLesson.lessonId,
            completedAllLessons = false,
            wordsLearned = 0,
            lesson = firstLesson
        )
    }

    private suspend fun ensureProgressSummaryExists(
        userId: String,
        dailyGoalMinutes: Int = 10,
        weeklyGoalDays: Int = 3
    ) {
        val defaultRow = ProgressSummaryRow(
            userId = userId,
            lastUpdateDate = today().toString(),
            dailyMinutesLearned = 0,
            dailyGoalMinutes = dailyGoalMinutes,
            weeklyDaysCompleted = 0,
            weeklyGoalDays = weeklyGoalDays,
            dayStreak = 0
        )

        supabase.from("ProgressSummary").upsert(defaultRow)
    }

    private suspend fun getFirstLesson(): LessonRefRow? {
        return supabase
            .from("Lesson")
            .select(columns = lessonColumns)
            .decodeList<LessonRefRow>()
            .minByOrNull { it.lessonId }
    }

    private suspend fun getNextLesson(currentLessonId: Long): LessonRefRow? {
        return supabase
            .from("Lesson")
            .select(columns = lessonColumns) {
                filter { gt("lesson_id", currentLessonId) }
            }
            .decodeList<LessonRefRow>()
            .minByOrNull { it.lessonId }
    }
}