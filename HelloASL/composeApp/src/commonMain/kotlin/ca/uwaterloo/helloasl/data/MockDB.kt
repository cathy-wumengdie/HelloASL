package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.userModel.*
import kotlinx.datetime.*
import ca.uwaterloo.helloasl.domain.learningModel.*
import ca.uwaterloo.helloasl.domain.translateModel.*
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import java.util.Objects.hash

class MockDB {
    private val yesterday = today().minus(DatePeriod(days = 1))
    private val twoDaysAgo = today().minus(DatePeriod(days = 2))
    private val threeDaysAgo = today().minus(DatePeriod(days = 3))
    private val users = mutableMapOf(
        "1" to User(id = "1", name = "Yanjin", email = "yanjin@gmail.com"),
        "2" to User(id = "2", name = "Erdo Long", email = "erdolong@gmail.com"),
        "3" to User(id = "3", name = "David", email = "david@gmail.com")
    )

    private val credentials = mutableMapOf(
        "1" to UserCredential(userId = "1", passwordHash = hash("1234")),
        "2" to UserCredential(userId = "2", passwordHash = hash("abc")),
        "3" to UserCredential(userId = "3", passwordHash = hash("abc123"))
    )

    val signs: List<ASLSign> = listOf(
        ASLSign(
            id = 1L,
            word = "Hello",
            description = "Greeting",
            videoUrls = listOf("files/video/hello.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 2L,
            word = "Thanks",
            description = "Gratitude",
            videoUrls = listOf("files/video/thankyou.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 3L,
            word = "Yes",
            description = "Affirmation",
            videoUrls = listOf("files/video/yes.mp4"),
            tags = setOf("basic")
        ),
        ASLSign(
            id = 4L,
            word = "No",
            description = "Negation",
            videoUrls = listOf("files/video/no.mp4"),
            tags = setOf("basic")
        )
    )

    val lessons: List<Lesson> = listOf(
        Lesson(
            id = 1L,
            title = "Basic Greetings",
            signIds = listOf(1, 2),
            category = "Beginner",
            locked = false
        ),
        Lesson(
            id = 2L,
            title = "Yes / No",
            signIds = listOf(3, 4),
            category = "Beginner",
            locked = true
        )
    )

    val modules: List<Module> = listOf(
        Module(
            id = 1L,
            title = "Unit 1: Basics",
            lessonIds = listOf(1, 2),
            category = "Beginner",
            locked = false
        )
    )

    private val progressSummary = mutableMapOf(
        "1" to ProgressSummary(
            userId = "1",
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

        "2" to ProgressSummary(
            userId = "2",
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

        "3" to ProgressSummary(
            userId = "3",
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

    private var userLearningProgress = mutableMapOf(
        "1" to UserLearningProgress(
            userId = "1",
            moduleId = 1L,
            lessonId = 1L,
            completedAllLessons = false,
            wordsLearned = 0
        ),

        "2" to UserLearningProgress(
            userId = "2",
            moduleId = 1L,
            lessonId = 2L,
            completedAllLessons = false,
            wordsLearned = 4
        ),

        "3" to UserLearningProgress(
            userId = "3",
            moduleId = 1L,
            lessonId = 1L,
            completedAllLessons = false,
            wordsLearned = 0
        )
    )

    private var userSession: UserSession? = null

    fun getAllUsers(): Map<String, User> = users.toMap()
    fun getAllUserIds(): Set<String> = users.keys.toSet()
    fun getUserById(userId: String): User? = users[userId]
    fun putUser(user: User) { users[user.id] = user }

    fun getCredential(userId: String): UserCredential? = credentials[userId]
    fun putCredential(credential: UserCredential) { credentials[credential.userId] = credential }

    fun getUserSession(): UserSession? = userSession
    fun setUserSession(session: UserSession?) { userSession = session }

    fun requireCurrentUserId(): String =
        userSession?.userId ?: error("User not logged in")

    fun getProgressSummary(userId: String): ProgressSummary? = progressSummary[userId]
    fun putProgressSummary(userId: String, summary: ProgressSummary) {
        progressSummary[userId] = summary
    }

    fun getUserLearningProgress(userId: String): UserLearningProgress? = userLearningProgress[userId]
    fun putUserLearningProgress(userId: String, progress: UserLearningProgress) {
        userLearningProgress[userId] = progress
    }

    fun logout() {
        userSession = null
    }

    // ------ Translate ------ (Fake, for Sprint 2 only)
    private val translateDictionary: Map<String, TranslateResult> = mapOf(
        "hello" to TranslateResult(query = "hello", videoUrls = listOf("files/video/hello.mp4")),
        "thanks" to TranslateResult(query = "thanks", videoUrls = listOf("files/video/thankyou.mp4")),
        "thank you" to TranslateResult(query = "thank you", videoUrls = listOf("files/video/thankyou.mp4")),
        "yes" to TranslateResult(query = "yes", videoUrls = listOf("files/video/yes.mp4")),
        "no" to TranslateResult(query = "no", videoUrls = listOf("files/video/no.mp4"))
    )

    private var nextHistoryId = 1
    private val translateHistory = mutableListOf(
        TranslateHistoryItem(id = nextHistoryId++, query = "Hello"),
        TranslateHistoryItem(id = nextHistoryId++, query = "Thanks")
    )

    fun searchWord(word: String): TranslateResult? {
        val key = word.trim().lowercase()
        if (key.isBlank()) return null
        return translateDictionary[key] ?: TranslateResult(query = word.trim())
    }

    fun getTranslateSearchHistory(): List<TranslateHistoryItem> {
        return translateHistory.asReversed()  // newest search should appear at the top
    }

    fun addTranslateHistory(word: String) {
        val clean = word.trim()
        if (clean.isBlank()) return
        translateHistory.add(TranslateHistoryItem(id = nextHistoryId++, query = clean))
    }

    fun clearTranslateHistory() {
        translateHistory.clear()
    }

    // Hard-coded recognition result
    fun recognizeAsl(): AslRecognitionResult {
        return AslRecognitionResult(recognizedText = "Hello", confidence = 0.86f)
    }

    private val starredItems = mutableMapOf(
        "1" to mutableListOf(
            StarItem(id = "cat", label = "Cat"),
            StarItem(id = "dog", label = "Dog"),
            StarItem(id = "fish", label = "Fish")
        )
    )
    fun getStarredItemsForUser(userId: String): List<StarItem> {
        return starredItems[userId]?.toList() ?: emptyList()
    }

    fun removeStarForUser(userId: String, itemId: String) {
        starredItems[userId]?.removeAll { it.id == itemId }
    }
}