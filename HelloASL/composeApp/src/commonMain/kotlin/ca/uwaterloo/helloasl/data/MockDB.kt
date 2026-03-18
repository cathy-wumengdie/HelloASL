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
            signId = 1L,
            lessonId = 1L,
            gloss = "Hello",
            videoUrl1 = "files/video/hello.mp4"
        ),
        ASLSign(
            signId = 2L,
            lessonId = 1L,
            gloss = "Thanks",
            videoUrl1 = "files/video/thankyou.mp4"
        ),
        ASLSign(
            signId = 3L,
            lessonId = 2L,
            gloss = "Yes",
            videoUrl1 = "files/video/yes.mp4"
        ),
        ASLSign(
            signId = 4L,
            lessonId = 2L,
            gloss = "No",
            videoUrl1 = "files/video/no.mp4"
        )
    )

    val lessons: List<Lesson> = listOf(
        Lesson(
            lessonId = 1L,
            moduleId = 1L,
            title = "Basic Greetings"
        ),
        Lesson(
            lessonId = 2L,
            moduleId = 1L,
            title = "Yes / No"
        )
    )

    val modules: List<Module> = listOf(
        Module(
            moduleId = 1L,
            title = "Basics",
            category = "Beginner"
        )
    )

    val quizChoices: List<QuizChoice> = listOf(
        QuizChoice(choiceId = 1L, signId = 1L, choiceText = "Hello", isCorrect = true),
        QuizChoice(choiceId = 2L, signId = 1L, choiceText = "Thanks", isCorrect = false),
        QuizChoice(choiceId = 3L, signId = 1L, choiceText = "Yes", isCorrect = false),

        QuizChoice(choiceId = 4L, signId = 2L, choiceText = "Thanks", isCorrect = true),
        QuizChoice(choiceId = 5L, signId = 2L, choiceText = "Hello", isCorrect = false),
        QuizChoice(choiceId = 6L, signId = 2L, choiceText = "No", isCorrect = false),

        QuizChoice(choiceId = 7L, signId = 3L, choiceText = "Yes", isCorrect = true),
        QuizChoice(choiceId = 8L, signId = 3L, choiceText = "No", isCorrect = false),
        QuizChoice(choiceId = 9L, signId = 3L, choiceText = "Hello", isCorrect = false),

        QuizChoice(choiceId = 10L, signId = 4L, choiceText = "No", isCorrect = true),
        QuizChoice(choiceId = 11L, signId = 4L, choiceText = "Yes", isCorrect = false),
        QuizChoice(choiceId = 12L, signId = 4L, choiceText = "Thanks", isCorrect = false)
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

    private val completedLessons: MutableSet<CompletedLesson> = mutableSetOf(
        CompletedLesson(userId = "2", lessonId = 1L)
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

    fun getCompletedLessonsForUser(userId: String): Set<CompletedLesson> =
        completedLessons.filter { it.userId == userId }.toSet()

    fun getCompletedLessonIdsForUser(userId: String): Set<Long> =
        completedLessons
            .asSequence()
            .filter { it.userId == userId }
            .map { it.lessonId }
            .toSet()

    fun isLessonCompleted(userId: String, lessonId: Long): Boolean =
        CompletedLesson(userId, lessonId) in completedLessons

    fun addCompletedLesson(userId: String, lessonId: Long): Boolean {
        return completedLessons.add(CompletedLesson(userId, lessonId))
    }

    fun logout() {
        userSession = null
    }

    // ------ Translate ------
    private var nextHistoryId = 1
    private val translateHistory = mutableListOf(
        TranslateHistoryItem(id = nextHistoryId++, query = "Hello"),
        TranslateHistoryItem(id = nextHistoryId++, query = "Thanks")
    )

    // Making use of Learning's ASL signs
    fun searchWord(word: String): ASLSign? {
        val key = word.trim()
        if (key.isBlank()) return null

        return when (key.lowercase()) {
            // "thank you" and "thanks" map to the same ASL sign
            "thank you" -> signs.firstOrNull { it.gloss.equals("Thanks", ignoreCase = true) }
            else -> signs.firstOrNull { it.gloss.equals(key, ignoreCase = true) }
        }
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

//    private val starredItems = mutableMapOf(
//        "1" to mutableListOf(
//            StarItem(
//                id = "cat",
//                signId = L,
//                label = "please",
//                videoUrl = "videos/cat.mp4",
//                tagName = "Favorites"
//            ),
//            StarItem(
//                id = "dog",
//                label = "hello",
//                videoUrl = "videos/dog.mp4",
//                tagName = "Practice"
//            ),
//            StarItem(
//                id = "fish",
//                label = "thank you",
//                videoUrl = "videos/fish.mp4",
//                tagName = "Review"
//            )
//        )
//    )
//    fun getStarredItemsForUser(userId: String): List<StarItem> {
//        return starredItems[userId]?.toList() ?: emptyList()
//    }
//
//    fun removeStarForUser(userId: String, itemId: String) {
//        starredItems[userId]?.removeAll { it.id == itemId }
//    }
}