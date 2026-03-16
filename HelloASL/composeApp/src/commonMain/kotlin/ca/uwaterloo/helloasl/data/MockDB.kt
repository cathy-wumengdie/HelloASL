package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameDate
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.isSameWeek
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
            signId = 1,
            lessonId = 1,
            gloss = "Hello",
            videoUrl1 = "files/video/hello.mp4"
        ),
        ASLSign(
            signId = 2,
            lessonId = 1,
            gloss = "Thanks",
            videoUrl1 = "files/video/thankyou.mp4"
        ),
        ASLSign(
            signId = 3,
            lessonId = 2,
            gloss = "Yes",
            videoUrl1 = "files/video/yes.mp4"
        ),
        ASLSign(
            signId = 4,
            lessonId = 2,
            gloss = "No",
            videoUrl1 = "files/video/no.mp4"
        )
    )

    val lessons: List<Lesson> = listOf(
        Lesson(
            lessonId = 1,
            moduleId = 1,
            title = "Basic Greetings"
        ),
        Lesson(
            lessonId = 2,
            moduleId = 1,
            title = "Yes / No"
        )
    )

    val modules: List<Module> = listOf(
        Module(
            moduleId = 1,
            title = "Unit 1: Basics",
            category = "Beginner"
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

    private var userLearningProgress = mutableMapOf(
        1 to UserLearningProgress(
            userId = 1,
            moduleId = 1,
            lessonId = 1,
            wordsLearned = 0,
            starredSigns = 12
        ),

        2 to UserLearningProgress(
            userId = 2,
            moduleId = 1,
            lessonId = 2,
            wordsLearned = 4,
            starredSigns = 1
        ),

        3 to UserLearningProgress(
            userId = 3,
            moduleId = 1,
            lessonId = 1,
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

    fun getUserLearningProgress(): UserLearningProgress {
        val userId = getUserId()
        return userLearningProgress[userId] ?: error("User learning progress not found")
    }

    private fun getLessonIdsForModule(moduleId: Int): List<Int> {
        return lessons
            .filter { it.moduleId == moduleId }
            .sortedBy { it.lessonId }
            .map { it.lessonId }
    }

    fun updateLearningProgress(): Boolean {
        val userId = getUserId()
        val learningProgress = getUserLearningProgress()

        // Already fully completed
        if (learningProgress.lessonId == -1) {
            return false
        }

        val sortedModules = modules.sortedBy { it.moduleId }
        val currentModuleIndex = sortedModules.indexOfFirst { it.moduleId == learningProgress.moduleId }
        if (currentModuleIndex == -1) {
            error("Module not found: ${learningProgress.moduleId}")
        }
        val currentModule = sortedModules[currentModuleIndex]
        val lessonIds = getLessonIdsForModule(currentModule.moduleId)
        if (lessonIds.isEmpty()) {
            return advanceToNextModuleFirstLesson(userId, learningProgress, sortedModules, currentModuleIndex)
        }
        val currentLessonIdx = lessonIds.indexOf(learningProgress.lessonId)
        if (currentLessonIdx == -1) {
            error("Invalid lesson id ${learningProgress.lessonId} for module ${learningProgress.moduleId}. lessonIds=$lessonIds")
        }
        val nextLessonIdx = currentLessonIdx + 1
        if (nextLessonIdx in lessonIds.indices) {
            userLearningProgress[userId] = learningProgress.copy(
                lessonId = lessonIds[nextLessonIdx]
            )
            return true
        }
        return advanceToNextModuleFirstLesson(userId, learningProgress, sortedModules, currentModuleIndex)
    }

    private fun advanceToNextModuleFirstLesson(
        userId: Int,
        learningProgress: UserLearningProgress,
        sortedModules: List<Module>,
        currentModuleIndex: Int
    ): Boolean {
        val nextModuleIndex = currentModuleIndex + 1
        if (nextModuleIndex > sortedModules.lastIndex) {
            val lastModule = sortedModules[currentModuleIndex]
            val completedProgress = learningProgress.copy(
                moduleId = lastModule.moduleId,
                lessonId = -1
            )
            userLearningProgress[userId] = completedProgress
            return false
        }
        val nextModule = sortedModules[nextModuleIndex]
        val nextLessonIds = getLessonIdsForModule(nextModule.moduleId)
        if (nextLessonIds.isEmpty()) {
            return advanceToNextModuleFirstLesson(
                userId,
                learningProgress,
                sortedModules,
                nextModuleIndex
            )
        }
        userLearningProgress[userId] = learningProgress.copy(
            moduleId = nextModule.moduleId,
            lessonId = nextLessonIds.first()
        )
        return true
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
        userLearningProgress[userId]?.let { progress ->
            userLearningProgress[userId] = progress.copy(wordsLearned = newWordsLearned)
        } ?: error("User learning progress not found")
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
        val firstModule = modules.minByOrNull { it.moduleId } ?: error("No modules available")
        val firstLessonId = getLessonIdsForModule(firstModule.moduleId).firstOrNull()
            ?: error("First module has no lessons")
        users[newUserId] = newUser
        credentials[newUserId] = UserCredential(newUser.id, hash(password))
        setProgressSummary(newUserId, newProgressSummary)
        userLearningProgress[newUserId] = UserLearningProgress(
            userId = newUserId,
            moduleId = firstModule.moduleId,
            lessonId = firstLessonId,
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
        1 to mutableListOf(
            StarItem(id = "cat", label = "Cat"),
            StarItem(id = "dog", label = "Dog"),
            StarItem(id = "fish", label = "Fish")
        )
    )

    fun getStarredItems(): List<StarItem> {
        val userId = getUserId()
        return starredItems[userId]?.toList() ?: emptyList()
    }

    fun removeStar(itemId: String) {
        val userId = getUserId()
        starredItems[userId]?.removeAll { it.id == itemId }

        val newCount = starredItems[userId]?.size ?: 0
        val progress = userLearningProgress[userId] ?: error("User learning progress not found")
        userLearningProgress[userId] = progress.copy(starredSigns = newCount)
    }
}