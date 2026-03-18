package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.authRepository.AuthRepository
import ca.uwaterloo.helloasl.data.authRepository.LoginResult
import ca.uwaterloo.helloasl.data.authRepository.SignUpResult
import ca.uwaterloo.helloasl.data.learningRepository.LearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.ProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.StarRepository
import ca.uwaterloo.helloasl.data.translateRepository.TranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.UserRepository
import ca.uwaterloo.helloasl.domain.learningModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.userModel.*
import ca.uwaterloo.helloasl.domain.translateModel.*
import ca.uwaterloo.helloasl.domain.starModel.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

data class Repositories(
    val auth: AuthRepository,
    val star: StarRepository,
    val user: UserRepository,
    val learning: LearningRepository,
    val translate: TranslateRepository,
    val progressTracker: ProgressTrackerRepository
)

class Model(
    private val repos: Repositories,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val starredSignIds: MutableSet<Long> = mutableSetOf()
    var pendingStarSignId: Long? by mutableStateOf(null)
        private set
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var showCreateTagInput by mutableStateOf(false)
    var newTagName by mutableStateOf("")

    var tagDialogVisible: Boolean by mutableStateOf(false)
        private set

    var availableStarTags: List<StarTag> by mutableStateOf(emptyList())
        private set

    private val lessonLocks: MutableMap<Long, Boolean> = mutableMapOf()
    private var lessonLocksInitialized = false
    private var lessonLocksUserId: String? = null

    private suspend fun ensureLessonLocksInitialized() {
        val currentUserId = repos.user.getUserLearningProgress().userId
        if (lessonLocksInitialized && lessonLocksUserId == currentUserId) return

        lessonLocks.clear()
        lessonLocksUserId = currentUserId

        val lessons = repos.learning.getLessons().sortedBy { it.lessonId }
        val completedIds = repos.user.getCompletedLessonIds()
        val maxCompleted = completedIds.maxOrNull() ?: 0L
        val firstLessonId = lessons.firstOrNull()?.lessonId
        val unlockThrough = if (maxCompleted > 0L) maxCompleted + 1 else 0L

        lessons.forEach { lesson ->
            val unlockedByCompletion = unlockThrough > 0L && lesson.lessonId <= unlockThrough
            val unlockedAsFirst = maxCompleted == 0L && lesson.lessonId == firstLessonId
            lessonLocks[lesson.lessonId] = !(unlockedByCompletion || unlockedAsFirst)
        }

        lessonLocksInitialized = true
    }

    suspend fun prepareLessonLocks() = withContext(ioDispatcher) {
        ensureLessonLocksInitialized()
    }

    private fun isLessonLockedInternal(lessonId: Long): Boolean =
        lessonLocks[lessonId] ?: false

    fun isLoggedIn(): Boolean {
        return repos.auth.isLoggedIn()
    }

    suspend fun getUser(): User = repos.user.getUser()

    suspend fun getUserLearningProgress(): UserLearningProgress =
        repos.user.getUserLearningProgress()

    suspend fun setLearningGoals(minutesPerDay: Int, daysPerWeek: Int): ProgressSummary {
        repos.user.updateLearningGoals(minutesPerDay, daysPerWeek)
        return repos.progressTracker.reevaluateProgressAfterGoalChange(
            dailyGoalMinutes = minutesPerDay,
            weeklyGoalDays = daysPerWeek
        )
    }

    suspend fun signup(name: String, email: String, password: String): SignUpResult =
        repos.auth.signup(name, email, password)

    suspend fun login(email: String, password: String): LoginResult =
        repos.auth.login(email, password)

    suspend fun logout(): Result<Unit> = repos.auth.logout()

    suspend fun getModules(): List<Module> = withContext(ioDispatcher) {
        repos.learning.getModules()
    }

    suspend fun getLessons(): List<Lesson> = withContext(ioDispatcher) {
        repos.learning.getLessons()
    }

    suspend fun getLessonsByModuleId(moduleId: Long): List<Lesson> = withContext(ioDispatcher) {
        repos.learning.getLessonsByModuleId(moduleId)
    }

    suspend fun getModule(id: Long): Module = withContext(ioDispatcher) {
        repos.learning.getModuleById(id)
    }

    suspend fun getLesson(lessonId: Long): Lesson = withContext(ioDispatcher) {
        repos.learning.getLessonById(lessonId)
    }

    fun unlockLesson(lessonId: Long) {
        lessonLocks[lessonId] = false
    }

    fun isLessonLocked(lessonId: Long): Boolean =
        isLessonLockedInternal(lessonId)

    suspend fun getSignCountForLesson(lessonId: Long): Int = withContext(ioDispatcher) {
        repos.learning.getSignsByLessonId(lessonId).size
    }

    suspend fun getSignsForLesson(lessonId: Long): List<ASLSign> = withContext(ioDispatcher) {
        repos.learning.getSignsByLessonId(lessonId)
    }

    suspend fun getStarredSigns(): List<ASLSign> = withContext(ioDispatcher) {
        val user = repos.user.getUser()
        val ids = repos.star.getStarredSignIds(user.id)
        repos.learning.getSignsByIds(ids)
    }

    suspend fun getQuizChoicesForSigns(signIds: List<Long>): Map<Long, List<QuizChoice>> =
        withContext(ioDispatcher) {
            if (signIds.isEmpty()) return@withContext emptyMap()

            repos.learning
                .getQuizChoicesBySignIds(signIds)
                .groupBy { it.signId }
        }

    suspend fun onLessonCompleted(completedLessonId: Long) = withContext(ioDispatcher) {
        val progress = getUserLearningProgress()

        if (progress.lessonId != completedLessonId) {
            return@withContext
        }

        val newlyCompleted = repos.user.completeLesson(completedLessonId)
        if (!newlyCompleted) {
            return@withContext
        }
        repos.user.updateLearningProgress()
    }

    fun isStarred(signId: Long): Boolean = signId in starredSignIds

    suspend fun toggleStar(signId: Long, tagId: Long): Boolean {
        val userId = repos.user.getUser().id

        return if (starredSignIds.contains(signId)) {
            starredSignIds.remove(signId)
            repos.star.removeStar(userId, signId)
            false
        } else {
            starredSignIds.add(signId)
            repos.star.addStar(userId, signId, tagId)
            true
        }
    }

    fun nextIndex(current: Int, total: Int): Int =
        if (total <= 0) 0 else (current + 1).coerceAtMost(total - 1)

    fun prevIndex(current: Int, total: Int): Int =
        if (total <= 0) 0 else (current - 1).coerceAtLeast(0)

    suspend fun getProgressSummary(): ProgressSummary =
        repos.progressTracker.getProgressSummary()

    suspend fun addLearningMinutes(minutes: Int): ProgressSummary =
        repos.progressTracker.addLearningMinutes(minutes)

    suspend fun translateWord(word: String): ASLSign? =
        repos.translate.searchWord(word)

    suspend fun getTranslateHistory(): List<TranslateHistoryItem> =
        repos.translate.getSearchHistory()

    suspend fun addTranslateHistory(word: String) =
        repos.translate.addHistory(word)

    suspend fun clearTranslateHistory() =
        repos.translate.clearHistory()

    suspend fun recognizeAsl(): AslRecognitionResult =
        repos.translate.recognizeAsl()

    suspend fun removeStar(userId: String, signId: Long) {
        repos.star.removeStar(userId, signId)
    }

    fun loadStarred(signIds: List<Long>) {
        starredSignIds.clear()
        starredSignIds.addAll(signIds)
    }

    suspend fun loadStarredFromRepo() = withContext(ioDispatcher) {
        val user = repos.user.getUser()
        val ids = repos.star.getStarredSignIds(user.id)

        starredSignIds.clear()
        starredSignIds.addAll(ids)
    }

    fun requestStarWithTag(signId: Long) {
        pendingStarSignId = signId

        scope.launch {
            val userId = repos.user.getUser().id
            availableStarTags = repos.star.getTags(userId)

            tagDialogVisible = true
        }
    }

    fun dismissTagDialog() {
        tagDialogVisible = false
        pendingStarSignId = null
        availableStarTags = emptyList()
    }

    suspend fun confirmStarWithTag(tagId: Long): Boolean {
        val signId = pendingStarSignId ?: return false
        val userId = repos.user.getUser().id

        val result = if (starredSignIds.contains(signId)) {
            starredSignIds.remove(signId)
            repos.star.removeStar(userId, signId)
            false
        } else {
            starredSignIds.add(signId)
            repos.star.addStar(userId, signId, tagId)
            true
        }

        tagDialogVisible = false
        pendingStarSignId = null

        return result
    }

    suspend fun createTag(name: String) {
        val userId = repos.user.getUser().id
        repos.star.createTag(userId, name)
        availableStarTags = repos.star.getTags(userId)
    }

    suspend fun getStarredItems(): List<StarItem> {
        val userId = getCurrentUserId()

        val rows = repos.star.getStarRows(userId)
        val signs = repos.learning.getSignsByIds(rows.map { it.signId })
        val tags = repos.star.getTags(userId)

        val signMap = signs.associateBy { it.signId }
        val tagMap = tags.associateBy { it.id }

        return rows.mapNotNull { row ->
            val sign = signMap[row.signId]
            val tag = tagMap[row.tagId]

            if (sign != null && tag != null) {
                StarItem(
                    signId = row.signId,
                    label = sign.gloss,
                    videoUrl = sign.videoUrl1,
                    tagName = tag.name
                )
            } else null
        }
    }

    suspend fun getCurrentUserId(): String {
        return repos.user.getUser().id
    }
}