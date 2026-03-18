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

    private val lessonLocks: MutableMap<Long, Boolean> = mutableMapOf()
    private var lessonLocksInitialized = false

    private suspend fun ensureLessonLocksInitialized() {
        if (lessonLocksInitialized) return

        val lessonsByModule = repos.learning.getLessons().groupBy { it.moduleId }
        lessonsByModule.values.forEach { lessons ->
            lessons.sortedBy { it.lessonId }.forEachIndexed { index, lesson ->
                lessonLocks[lesson.lessonId] = index != 0
            }
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

    suspend fun setLearningGoals(minutesPerDay: Int, daysPerWeek: Int) =
        repos.user.updateLearningGoals(minutesPerDay, daysPerWeek)

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
        repos.learning.getSignsByIds(starredSignIds.toList())
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

    fun toggleStar(signId: Long): Boolean =
        if (starredSignIds.remove(signId)) {
            false
        } else {
            starredSignIds.add(signId)
            true
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

    fun getStarredItems(): List<StarItem> {
        return repos.star.getStarredItems()
    }

    fun removeStar(itemId: String) {
        repos.star.removeStar(itemId)
    }
}