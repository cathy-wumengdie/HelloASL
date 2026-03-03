package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameDate
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameWeek
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.userModel.*
import kotlinx.datetime.*
import ca.uwaterloo.helloasl.domain.learning.*
import java.util.Objects.hash

class MockDB {
    private val today = today()
    private val yesterday = today.minus(DatePeriod(days = 1))
    private val twoDaysAgo = today.minus(DatePeriod(days = 2))
    private val threeDaysAgo = today.minus(DatePeriod(days = 3))
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
            id = 0,
            word = "Hello",
            description = "Greeting",
            videoUrls = listOf("files/video/hello.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 1,
            word = "Thanks",
            description = "Gratitude",
            videoUrls = listOf("files/video/thankyou.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 2,
            word = "Yes",
            description = "Affirmation",
            videoUrls = listOf("files/video/yes.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 3,
            word = "No",
            description = "Negation",
            videoUrls = listOf("files/video/no.mp4"),
            tags = setOf("basic")
        )
    )

    val lessons: List<Lesson> = listOf(
        Lesson(
            id = 0,
            title = "Basic Greetings",
            signIds = listOf(0, 1),
            category = "Beginner",
            locked = false
        ),
        Lesson(
            id = 1,
            title = "Yes / No",
            signIds = listOf(2, 3),
            category = "Beginner",
            locked = true
        )
    )

    val modules: List<Module> = listOf(
        Module(
            id = 0,
            title = "Unit 1: Basics",
            lessonIds = listOf(0, 1),
            category = "Beginner",
            locked = false
        )
    )

    var userSession: UserSession? = null

    private val progressSummary = mutableMapOf(
        1 to ProgressSummary(
            userId = 1,
            date = today,
            dailyProgress = DailyProgress(
                minutesLearned = 20,
                dailyGoalMinutes = 15
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 3,
                weeklyGoalDays = 3
            ),
            dayStreakState = DayStreakState(
                lastLearnedDate = today,
                currentStreak = 7
            )
        ),

        2 to ProgressSummary(
            userId = 2,
            date = today,
            dailyProgress = DailyProgress(
                minutesLearned = 4,
                dailyGoalMinutes = 5
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 1,
                weeklyGoalDays = 2
            ),
            dayStreakState = DayStreakState(
                lastLearnedDate = yesterday,
                currentStreak = 1
            )
        ),

        3 to ProgressSummary(
            userId = 3,
            date = today,
            dailyProgress = DailyProgress(
                minutesLearned = 35,
                dailyGoalMinutes = 30
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 2,
                weeklyGoalDays = 5
            ),
            dayStreakState = DayStreakState(
                lastLearnedDate = threeDaysAgo,
                currentStreak = 3
            )
        )
    )

    private var userProfiles = mutableMapOf(
        1 to UserProfile(
            userId = 1,
            progressSummary = progressSummary[1]!!,
            learningProgress = LearningProgress(module = 2, lesson = 3),
            wordsLearned = 40,
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
            learningProgress = LearningProgress(module = 2, lesson = 1),
            wordsLearned = 28,
            starredSigns = 10
        )
    )

    // daily goal record on the last learned date per user
    private val lastLearnedDateDailyGoalRecord = mutableMapOf<Int, LastLearnedDatesGoalsRecords>(
        1 to LastLearnedDatesGoalsRecords(
            userId = 1,
            lastLearnedDate = today,
            isDailyGoalCompleted = true,
            learnedDatesCountInWeek = 3,
            isWeeklyGoalCompleted = true
        ),
        2 to LastLearnedDatesGoalsRecords(
            userId = 2,
            lastLearnedDate = yesterday,
            isDailyGoalCompleted = false,
            learnedDatesCountInWeek = 1,
            isWeeklyGoalCompleted = false
        ),
        3 to LastLearnedDatesGoalsRecords(
            userId = 3,
            lastLearnedDate = threeDaysAgo,
            isDailyGoalCompleted = true,
            learnedDatesCountInWeek = 2,
            isWeeklyGoalCompleted = false
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

    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        val userId = getUserId()
        val ps = refreshProgressSummary()
        val updated = ps.copy(
            dailyProgress = ps.dailyProgress.copy(dailyGoalMinutes = minutesPerDay),
            weeklyProgress = ps.weeklyProgress.copy(weeklyGoalDays = daysPerWeek)
        )
        setProgressSummary(userId, updated)
    }

    fun signup(name: String, email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()

        // reject duplicate emails
        if (users.values.any { it.email.lowercase() == cleanEmail }) return false

        val newUserId = (users.keys.maxOrNull() ?: 0) + 1
        val newUser = User(id = newUserId, name = name.trim(), email = cleanEmail)
        val newProgressSummary = ProgressSummary(
            userId = newUserId,
            date = today,
            dailyProgress = DailyProgress(
                minutesLearned = 0,
                dailyGoalMinutes = 0
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 0,
                weeklyGoalDays = 0
            ),
            dayStreakState = DayStreakState(
                lastLearnedDate = null,
                currentStreak = 0
            )
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
        lastLearnedDateDailyGoalRecord[newUserId] = LastLearnedDatesGoalsRecords(
            userId = newUserId,
            lastLearnedDate = today(),
            isDailyGoalCompleted = false,
            learnedDatesCountInWeek = 0,
            isWeeklyGoalCompleted = false
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

    fun refreshProgressSummary(): ProgressSummary {
        val userId = getUserId()
        val t = today()
        val existing = progressSummary[userId] ?: error("Progress summary not found for user $userId")

        // If still today, nothing to refresh
        if (isSameDate(existing.date, t)) return existing

        // If date changed, reset daily minutes
        var refreshed = existing.copy(
            date = t,
            dailyProgress = existing.dailyProgress.copy(minutesLearned = 0)
        )

        // If week changed, reset weekly daysCompleted
        if (!isSameWeek(existing.date, t)) {
            refreshed = refreshed.copy(
                weeklyProgress = existing.weeklyProgress.copy(daysCompleted = 0)
            )
        }
        setProgressSummary(userId, refreshed)
        return refreshed
    }

    private fun setProgressSummary(userId: Int, ps: ProgressSummary) {
        progressSummary[userId] = ps
        val profile = userProfiles[userId] ?: return
        userProfiles[userId] = profile.copy(progressSummary = ps)
    }

    private fun updateWeeklyProgress(
        userId: Int,
        today: LocalDate,
        before: ProgressSummary,    // the progress state before minutes added
        after: ProgressSummary      // the progress state after minutes added
    ): ProgressSummary {
        // Only trigger when user complete the daily goal
        if (before.dailyProgress.isDailyGoalMet) return after
        if (!after.dailyProgress.isDailyGoalMet) return after

        // If already counted today in records, then return after
        val record = lastLearnedDateDailyGoalRecord[userId]
        val alreadyRecordedToday = record != null && record.lastLearnedDate == today && record.isDailyGoalCompleted
        if (alreadyRecordedToday) return after

        // ---- Weekly increment rules ----
        val sameWeekAsRecord = record != null && isSameWeek(record.lastLearnedDate, today)
        val newLearnedDatesCountInWeek = when {
            record == null -> 1
            sameWeekAsRecord -> record.learnedDatesCountInWeek + 1
            else -> 1 // new week
        }

        val weeklyGoalDays = after.weeklyProgress.weeklyGoalDays
        val newIsWeeklyGoalCompleted = (weeklyGoalDays > 0 && newLearnedDatesCountInWeek >= weeklyGoalDays)

        // Update record table (supposed to update at EOD, but we update now for mock)
        lastLearnedDateDailyGoalRecord[userId] = LastLearnedDatesGoalsRecords(
            userId = userId,
            lastLearnedDate = today,
            isDailyGoalCompleted = true,
            learnedDatesCountInWeek = newLearnedDatesCountInWeek,
            isWeeklyGoalCompleted = newIsWeeklyGoalCompleted
        )

        // Update weeklyProgress in summary
        val newWeekly = after.weeklyProgress.copy(
            daysCompleted = newLearnedDatesCountInWeek
        )

        return after.copy(
            weeklyProgress = newWeekly
        )
    }

    fun addLearningMinutes(minutes: Int) {
        require(minutes > 0) { "minutes must be > 0" }
        val userId = getUserId()
        val t = today()
        // Ensure we have initialized summary for today
        val current = refreshProgressSummary()
        // Update minutes learned today
        val newMinutes = current.dailyProgress.minutesLearned + minutes
        val newDaily = current.dailyProgress.copy(minutesLearned = newMinutes)
        // If the goal is met, update streak
        val newStreakState = updateDayStreak(
            state = current.dayStreakState,
            today = t,
            isDailyGoalCompleted = newDaily.isDailyGoalMet
        )
        // Save back
        var updated = current.copy(
            date = t,
            dailyProgress = newDaily,
            dayStreakState = newStreakState
        )
        // Met the daily goal, update weekly + lastLearnedDatesGoalsRecords once
        updated = updateWeeklyProgress(
            userId = userId,
            today = t,
            before = current,
            after = updated
        )
        setProgressSummary(userId, updated)
    }

}