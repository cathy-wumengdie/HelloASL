package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.authRepository.AuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.LearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.ProgressTrackerRepository
import ca.uwaterloo.helloasl.data.userRepository.UserRepository
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

// Repository bundle
// auth / user / learning / ...
data class Repositories(
    val auth: AuthRepository,
    val user: UserRepository,
    val learning: LearningRepository,
    val progressTracker: ProgressTrackerRepository
)

class Model(private val repos: Repositories) {
    private val starredSignIds: MutableSet<Int> = mutableSetOf()
    // maintain lesson lock state within the session
    private val lessonLocks: MutableMap<Int, Boolean> =
        repos.learning.getLessons().associate { it.id to it.locked }.toMutableMap()

    private fun applyLessonLocks(lesson: Lesson): Lesson =
        lesson.copy(locked = lessonLocks[lesson.id] ?: lesson.locked)

    // user & auth
    fun getUser(): User = repos.user.getUser()
    fun getUserProfile(): UserProfile = repos.user.getUserProfile()
    fun setLearningGoals(minutesPerDay: Int, daysPerWeek: Int) = repos.user.updateLearningGoals(minutesPerDay, daysPerWeek)
    fun signup(name: String, email: String, password: String): Boolean = repos.auth.signup(name, email, password)
    fun login(email: String, password: String): Boolean = repos.auth.login(email, password)
    fun logout() = repos.auth.logout()

    // learning: modules / lessons / signs
    fun getModules(): List<Module> = repos.learning.getModules()
    fun getLessons(): List<Lesson> = repos.learning.getLessons().map(::applyLessonLocks)
    fun getLesson(lessonId: Int): Lesson? = repos.learning.getLessonById(lessonId)?.let(::applyLessonLocks)
    fun unlockLesson(lessonId: Int) { lessonLocks[lessonId] = false }
    fun getSignsForLesson(lessonId: Int): List<ASLSign> {
        val lesson = repos.learning.getLessonById(lessonId) ?: return emptyList()
        return repos.learning.getSignsByIds(lesson.signIds)
    }

    // starred
    fun isStarred(signId: Int): Boolean = signId in starredSignIds
    fun getStarredSigns(): List<ASLSign> = repos.learning.getSignsByIds(starredSignIds.toList())
    fun toggleStar(signId: Int): Boolean = if (starredSignIds.remove(signId)) false else { starredSignIds.add(signId); true }

    // quiz helpers
    fun nextIndex(current: Int, total: Int): Int = if (total <= 0) 0 else (current + 1).coerceAtMost(total - 1)
    fun prevIndex(current: Int, total: Int): Int = if (total <= 0) 0 else (current - 1).coerceAtLeast(0)

    // progress tracker
    fun getProgressSummary(): ProgressSummary = repos.progressTracker.getProgressSummary()
    fun addLearningMinutes(minutes: Int): ProgressSummary = repos.progressTracker.addLearningMinutes(minutes)
}