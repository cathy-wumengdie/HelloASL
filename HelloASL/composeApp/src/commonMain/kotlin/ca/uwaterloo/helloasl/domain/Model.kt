package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.authRepository.AuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.LearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.ProgressTrackerRepository
import ca.uwaterloo.helloasl.data.userRepository.UserRepository
import ca.uwaterloo.helloasl.data.translateRepository.TranslateRepository
import ca.uwaterloo.helloasl.domain.learningModel.*
import ca.uwaterloo.helloasl.domain.trackingModel.*
import ca.uwaterloo.helloasl.domain.userModel.*
import ca.uwaterloo.helloasl.domain.translateModel.*
import ca.uwaterloo.helloasl.domain.starModel.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Repository bundle
// auth / user / learning / ...
data class Repositories(
    val auth: AuthRepository,
    val user: UserRepository,
    val learning: LearningRepository,
    val translate: TranslateRepository,
    val progressTracker: ProgressTrackerRepository
)

class Model(
    private val repos: Repositories,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val starredSignIds: MutableSet<Int> = mutableSetOf()

    private val lessonLocks: MutableMap<Int, Boolean> = mutableMapOf()
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

    private fun isLessonLockedInternal(lessonId: Int): Boolean = lessonLocks[lessonId] ?: false

    // user & auth
    fun getUser(): User = repos.user.getUser()
    fun getUserLearningProgress(): UserLearningProgress = repos.user.getUserLearningProgress()
    fun setLearningGoals(minutesPerDay: Int, daysPerWeek: Int) =
        repos.user.updateLearningGoals(minutesPerDay, daysPerWeek)

    suspend fun getNumberOfWordsLearned(): Int = withContext(ioDispatcher) {
        val learningProgress = getUserLearningProgress()
        val currentModuleId = learningProgress.moduleId
        val currentLessonId = learningProgress.lessonId
        val modules = repos.learning.getModules().sortedBy { it.moduleId }
        val lessonsByModule = repos.learning.getLessons().groupBy { it.moduleId }
        val currentModuleIndex = modules.indexOfFirst { it.moduleId == currentModuleId }
        if (currentModuleIndex == -1) error("Module not found: $currentModuleId")
        val learnedSignIds = mutableSetOf<Int>()

        // 1) Add all signs from modules before current module
        for (module in modules.take(currentModuleIndex)) {
            val lessons = lessonsByModule[module.moduleId].orEmpty().sortedBy { it.lessonId }
            for (lesson in lessons) {
                repos.learning.getSignsByLessonId(lesson.lessonId).forEach { learnedSignIds.add(it.signId) }
            }
        }

        // 2) Add signs from lessons before current lesson in current module
        val currentModuleLessons = lessonsByModule[currentModuleId].orEmpty().sortedBy { it.lessonId }
        val currentLessonIndex = currentModuleLessons.indexOfFirst { it.lessonId == currentLessonId }
        val lessonsToCount = if (currentLessonId == -1 || currentLessonIndex == -1) {
            currentModuleLessons
        } else {
            currentModuleLessons.take(currentLessonIndex)
        }
        for (lesson in lessonsToCount) {
            repos.learning.getSignsByLessonId(lesson.lessonId).forEach { learnedSignIds.add(it.signId) }
        }
        learnedSignIds.size
    }

    fun signup(name: String, email: String, password: String): Boolean = repos.auth.signup(name, email, password)
    fun login(email: String, password: String): Boolean = repos.auth.login(email, password)
    fun logout() = repos.auth.logout()

    // learning: modules / lessons / signs
    suspend fun getModules(): List<Module> = withContext(ioDispatcher) { repos.learning.getModules() }
    suspend fun getLessons(): List<Lesson> = withContext(ioDispatcher) { repos.learning.getLessons() }
    suspend fun getLessonsByModuleId(moduleId: Int): List<Lesson> = withContext(ioDispatcher) {
        repos.learning.getLessonsByModuleId(moduleId)
    }
    suspend fun getModule(id: Int): Module = withContext(ioDispatcher) { repos.learning.getModuleById(id) }
    suspend fun getLesson(lessonId: Int): Lesson = withContext(ioDispatcher) { repos.learning.getLessonById(lessonId) }
    fun unlockLesson(lessonId: Int) {
        lessonLocks[lessonId] = false
    }
    fun isLessonLocked(lessonId: Int): Boolean = isLessonLockedInternal(lessonId)
    suspend fun getSignCountForLesson(lessonId: Int): Int = withContext(ioDispatcher) {
        repos.learning.getSignsByLessonId(lessonId).size
    }

    suspend fun getSignsForLesson(lessonId: Int): List<ASLSign> = withContext(ioDispatcher) {
        repos.learning.getSignsByLessonId(lessonId)
    }

    suspend fun getQuizChoicesForSigns(signIds: List<Int>): Map<Int, List<QuizChoice>> = withContext(ioDispatcher) {
        if (signIds.isEmpty()) return@withContext emptyMap()
        repos.learning
            .getQuizChoicesBySignIds(signIds)
            .groupBy { it.signId.toInt() }
    }

    suspend fun onLessonCompleted(completedLessonId: Int) = withContext(ioDispatcher) {
        val progress = getUserLearningProgress()
        // Ignore repeated completion of an old lesson or after all lessons are done
        if (progress.lessonId != completedLessonId) {
            return@withContext
        }
        // 1) advance learning progress first
        val advanced = repos.user.updateLearningProgress()
        // 2) recompute words learned based on updated progress
        val newWordsLearned = getNumberOfWordsLearned()
        // 3) persist to profile
        repos.user.updateWordsLearned(newWordsLearned)

        if (!advanced) {
            // no modules and lessons left to advance => all lessons completed
            return@withContext
        }
    }

    // starred
    fun isStarred(signId: Int): Boolean = signId in starredSignIds
    suspend fun getStarredSigns(): List<ASLSign> = withContext(ioDispatcher) {
        repos.learning.getSignsByIds(starredSignIds.toList())
    }
    fun toggleStar(signId: Int): Boolean = if (starredSignIds.remove(signId)) false else {
        starredSignIds.add(signId); true
    }

    // quiz helpers
    fun nextIndex(current: Int, total: Int): Int = if (total <= 0) 0 else (current + 1).coerceAtMost(total - 1)
    fun prevIndex(current: Int, total: Int): Int = if (total <= 0) 0 else (current - 1).coerceAtLeast(0)

    // progress tracker
    fun getProgressSummary(): ProgressSummary = repos.progressTracker.getProgressSummary()
    fun addLearningMinutes(minutes: Int): ProgressSummary = repos.progressTracker.addLearningMinutes(minutes)

    // translate
    fun translateWord(word: String): TranslateResult? = repos.translate.searchWord(word)
    fun getTranslateHistory(): List<TranslateHistoryItem> = repos.translate.getSearchHistory()
    fun addTranslateHistory(word: String) = repos.translate.addHistory(word)
    fun clearTranslateHistory() = repos.translate.clearHistory()
    fun recognizeAsl(): AslRecognitionResult = repos.translate.recognizeAsl()

    fun getStarredItems(): List<StarItem> {
        return repos.user.getStarredItems()
    }

    fun removeStar(itemId: String) {
        repos.user.removeStar(itemId)
    }
}