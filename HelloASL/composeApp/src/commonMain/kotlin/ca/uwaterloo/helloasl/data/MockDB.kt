package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameDate
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameWeek
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.userModel.*
import kotlinx.datetime.*
import ca.uwaterloo.helloasl.domain.learningModel.*
import java.util.Objects.hash

class MockDB {
    private val yesterday = today().minus(DatePeriod(days = 1))
    private val twoDaysAgo = today().minus(DatePeriod(days = 2))
    private val threeDaysAgo = today().minus(DatePeriod(days = 3))
    private val users = mutableMapOf(
        1 to User(id = 1, name = "Yanjin", email = "yanjin@gmail.com"),
        2 to User(id = 2, name = "Erdo Long", email = "erdolong@gmail.com"),
        3 to User(id = 3, name = "David", email = "david@gmail.com")
    )

    private val credentials = mutableMapOf(
        1 to UserCredential(userId = 1, passwordHash = hash("1234")),
        2 to UserCredential(userId = 2, passwordHash = hash("abc")),
        3 to UserCredential(userId = 3, passwordHash = hash("abc123"))
    )

    val signs: List<ASLSign> = listOf(
        ASLSign(
            id = 1,
            word = "Hello",
            description = "Greeting",
            videoUrls = listOf("files/video/hello.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 2,
            word = "Thanks",
            description = "Gratitude",
            videoUrls = listOf("files/video/thankyou.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 3,
            word = "Yes",
            description = "Affirmation",
            videoUrls = listOf("files/video/yes.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 4,
            word = "No",
            description = "Negation",
            videoUrls = listOf("files/video/no.mp4"),
            tags = setOf("basic")
        )
    )

    val lessons: List<Lesson> = listOf(
        Lesson(
            id = 1,
            title = "Basic Greetings",
            signIds = listOf(1, 2),
            category = "Beginner",
            locked = false
        ),
        Lesson(
            id = 2,
            title = "Yes / No",
            signIds = listOf(3, 4),
            category = "Beginner",
            locked = true
        )
    )

    val modules: List<Module> = listOf(
        Module(
            id = 1,
            title = "Unit 1: Basics",
            lessonIds = listOf(1, 2),
            category = "Beginner",
            locked = false
        )
    )

    var userSession: UserSession? = null

    private val progressSummary = mutableMapOf(
        1 to ProgressSummary(
            userId = 1,
            date = today(),
            dailyProgress = DailyProgress(
                minutesLearned = 20,
                lastDailyGoalCompletedDate = today(),
                dailyGoalMinutes = 15
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 3,
                lastCreditedDate = today(),
                weeklyGoalDays = 3
            ),
            dayStreak = 7
        ),

        2 to ProgressSummary(
            userId = 2,
            date = today(),
            dailyProgress = DailyProgress(
                minutesLearned = 4,
                lastDailyGoalCompletedDate = yesterday,
                dailyGoalMinutes = 5
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 1,
                lastCreditedDate = yesterday,
                weeklyGoalDays = 2
            ),
            dayStreak = 1
        ),

        3 to ProgressSummary(
            userId = 3,
            date = today(),
            dailyProgress = DailyProgress(
                minutesLearned = 35,
                lastDailyGoalCompletedDate = threeDaysAgo,
                dailyGoalMinutes = 30
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 2,
                lastCreditedDate = threeDaysAgo,
                weeklyGoalDays = 5
            ),
            dayStreak = 3
        )
    )

    private var userProfiles = mutableMapOf(
        1 to UserProfile(
            userId = 1,
            progressSummary = progressSummary[1]!!,
            learningProgress = LearningProgress(module = 1, lesson = 1),
            wordsLearned = 0,
            starredSigns = 12
        ),

        2 to UserProfile(
            userId = 2,
            progressSummary = progressSummary[2]!!,
            learningProgress = LearningProgress(module = 1, lesson = 2),
            wordsLearned = 4,
            starredSigns = 1
        ),

        3 to UserProfile(
            userId = 3,
            progressSummary = progressSummary[3]!!,
            learningProgress = LearningProgress(module = 1, lesson = 1),
            wordsLearned = 0,
            starredSigns = 10
        )
    )

    private fun getUserId(): Int {
        return userSession?.userId ?: throw IllegalStateException("User not logged in")
    }

    // this function is only for testing if the new user id is unique
    fun getAllUserIds(): Set<Int> {
        return users.keys.toSet()
    }

    fun getUser(): User {
        val userId = getUserId()
        return users[userId] ?: error("User not found")
    }

    fun getUserProfile(): UserProfile {
        val userId = getUserId()
        return userProfiles[userId] ?: error("Profile not found")
    }

    fun updateLearningProgress(moduleId: Int, lessonId: Int) {
        val userId = getUserId()
        userProfiles[userId] = userProfiles[userId]!!.copy(learningProgress = LearningProgress(moduleId, lessonId))
    }

    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        val userId = getUserId()
        val ps = refreshProgressSummary()
        val updated = ps.copy(
            dailyProgress = ps.dailyProgress.copy(dailyGoalMinutes = minutesPerDay),
            weeklyProgress = ps.weeklyProgress.copy(weeklyGoalDays = daysPerWeek)
        )
        setProgressSummary(userId, updated)
    }

    fun updateWordsLearned(newWordsLearned: Int) {
        val userId = getUserId()
        userProfiles[userId] = userProfiles[userId]!!.copy(wordsLearned = newWordsLearned)
    }

    fun signup(name: String, email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()

        // reject duplicate emails
        if (users.values.any { it.email.lowercase() == cleanEmail }) return false

        val newUserId = (users.keys.maxOrNull() ?: 0) + 1
        val newUser = User(id = newUserId, name = name.trim(), email = cleanEmail)
        val newProgressSummary = ProgressSummary(
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

        users[newUserId] = newUser
        credentials[newUserId] = UserCredential(newUser.id, hash(password))
        setProgressSummary(newUserId, newProgressSummary)
        userProfiles[newUserId] = UserProfile(
            userId = newUserId,
            progressSummary = newProgressSummary,
            learningProgress = LearningProgress(module = 1, lesson = 1),
            wordsLearned = 0,
            starredSigns = 0
        )

        // auto-login after signup
        userSession = UserSession(
            userId = newUser.id,
            userName = newUser.name,
            email = newUser.email,
            loginTime = System.currentTimeMillis()
        )
        return true
    }

    fun login(email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val user = users.values.find { it.email.lowercase() == cleanEmail } ?: return false
        val cred = credentials[user.id] ?: return false

        if (cred.passwordHash != hash(password)) return false

        userSession = UserSession(
            userId = user.id,
            userName = user.name,
            email = user.email,
            loginTime = System.currentTimeMillis()
        )
        return true
    }

    fun logout() {
        userSession = null
    }

    private fun refreshProgressSummary(): ProgressSummary {
        val userId = getUserId()
        val t = today()
        val existing = progressSummary[userId] ?: error("Progress summary not found for user $userId")

        // If still today(), nothing to refresh
        if (isSameDate(existing.date, t)) return existing

        // If date changed, reset daily minutes
        var refreshed = existing.copy(
            date = t,
            dailyProgress = existing.dailyProgress.copy(minutesLearned = 0)
        )

        // If week changed, reset weekly daysCompleted
        if (!isSameWeek(existing.date, t)) {
            refreshed = refreshed.copy(
                weeklyProgress = existing.weeklyProgress.copy(
                    daysCompleted = 0,
                    lastCreditedDate = null
                )
            )
        }
        setProgressSummary(userId, refreshed)
        return refreshed
    }

    fun getProgressSummary(): ProgressSummary = refreshProgressSummary()

    // Update the progress summary table and in the user profile
    private fun setProgressSummary(userId: Int, ps: ProgressSummary) {
        progressSummary[userId] = ps
        val profile = userProfiles[userId] ?: return
        userProfiles[userId] = profile.copy(progressSummary = ps)
    }

    private fun updateWeeklyProgress(
        today: LocalDate,
        before: ProgressSummary,    // the progress summary before minutes added
        after: ProgressSummary      // the progress summary after minutes added
    ): ProgressSummary {
        // Only triggers when user complete the daily goal
        if (before.dailyProgress.isDailyGoalMet) return after
        if (!after.dailyProgress.isDailyGoalMet) return after

        // Prevent double counting today
        if (after.weeklyProgress.lastCreditedDate == today) return after

        val newWeekly = after.weeklyProgress.copy(
            daysCompleted = after.weeklyProgress.daysCompleted + 1,
            lastCreditedDate = today
        )

        return after.copy(weeklyProgress = newWeekly)
    }

    fun addLearningMinutes(minutes: Int) {
        require(minutes > 0) { "minutes must be > 0" }
        val userId = getUserId()
        val t = today()
        // Ensure we have initialized summary for today
        val current = refreshProgressSummary()
        // 1) Update minutes learned today
        val newMinutes = current.dailyProgress.minutesLearned + minutes
        var newDailyProgress = current.dailyProgress.copy(minutesLearned = newMinutes)
        // 2) If the goal is met, update streak + lastDailyGoalCompletedDate
        val (newStreak, newLastDailyGoalCompletedDate) = updateDayStreak(
            currentStreak = current.dayStreak,
            lastDailyGoalCompletedDate = current.dailyProgress.lastDailyGoalCompletedDate,
            today = t,
            isDailyGoalCompleted = newDailyProgress.isDailyGoalMet
        )
        newDailyProgress = newDailyProgress.copy(
            lastDailyGoalCompletedDate = newLastDailyGoalCompletedDate
        )
        // Save to progress summary
        var updated = current.copy(
            date = t,
            dailyProgress = newDailyProgress,
            dayStreak = newStreak
        )
        // 3) Update weekly progress only when daily goal is met and haven't counted today yet
        updated = updateWeeklyProgress(
            today = t,
            before = current,
            after = updated
        )
        setProgressSummary(userId, updated)
    }
}