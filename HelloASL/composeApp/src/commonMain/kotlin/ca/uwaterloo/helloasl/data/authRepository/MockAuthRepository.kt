package ca.uwaterloo.helloasl.data.authRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserCredential
import ca.uwaterloo.helloasl.domain.userModel.UserLearningProgress
import java.util.Objects.hash
import java.util.UUID

class MockAuthRepository(
    private val db: MockDB
) : AuthRepository {
    override suspend fun signup(
        name: String,
        email: String,
        password: String
    ): SignUpResult {
        return try {
            val cleanEmail = email.trim().lowercase()
            if (db.getAllUsers().values.any { it.email.lowercase() == cleanEmail }) {
                throw IllegalStateException("Email already exists")
            }
            val newUserId = UUID.randomUUID().toString()
            val newUser = User(
                id = newUserId,
                name = name.trim(),
                email = cleanEmail
            )
            val firstModule = db.modules.minByOrNull { it.moduleId }
                ?: error("No modules available")

            val firstLessonId = db.lessons
                .filter { it.moduleId == firstModule.moduleId }
                .sortedBy { it.lessonId }
                .firstOrNull()
                ?.lessonId
                ?: error("First module has no lessons")

            db.putUser(newUser)
            db.putCredential(
                UserCredential(
                    userId = newUserId,
                    passwordHash = hash(password)
                )
            )
            db.putProgressSummary(
                newUserId,
                ProgressSummary(
                    userId = newUserId,
                    date = today(),
                    dailyProgress = DailyProgress(
                        minutesLearned = 0,
                        lastDailyGoalCompletedDate = null,
                        dailyGoalMinutes = 0
                    ),
                    weeklyProgress = WeeklyProgress(
                        daysCompleted = 0,
                        lastCreditedDate = null,
                        weeklyGoalDays = 0
                    ),
                    dayStreak = 0
                )
            )
            db.putUserLearningProgress(
                newUserId,
                UserLearningProgress(
                    userId = newUserId,
                    moduleId = firstModule.moduleId,
                    lessonId = firstLessonId,
                    completedAllLessons = false,
                    wordsLearned = 0
                )
            )
            SignUpResult.NeedsEmailVerification
        } catch (e: Throwable) {
            SignUpResult.Failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): LoginResult {
        return try {
            val cleanEmail = email.trim().lowercase()
            val user = db.getAllUsers().values.find { it.email.lowercase() == cleanEmail }
                ?: throw IllegalStateException("User not found")
            val credential = db.getCredential(user.id)
                ?: throw IllegalStateException("Credential not found")
            if (credential.passwordHash != hash(password)) {
                throw IllegalStateException("Invalid password")
            }

            // Mock now behaves like Supabase with email confirmation enabled:
            // signup creates the user, but login is blocked until email is verified.
            LoginResult.EmailNotVerified
        } catch (e: Throwable) {
            LoginResult.Failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        db.setUserSession(null)
    }
    override suspend fun getCurrentUserId(): String? {
        return db.getUserSession()?.userId
    }
    override fun isLoggedIn(): Boolean {
        return db.getUserSession() != null
    }
}