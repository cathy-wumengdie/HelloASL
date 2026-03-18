package ca.uwaterloo.helloasl.data.userRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.userModel.*

class MockUserRepository(private val db: MockDB) : UserRepository {

    override suspend fun getUser(): User {
        val userId = db.requireCurrentUserId()
        return db.getUserById(userId) ?: error("User not found")
    }

    override suspend fun getUserLearningProgress(): UserLearningProgress {
        val userId = db.requireCurrentUserId()
        return db.getUserLearningProgress(userId) ?: error("User learning progress not found")
    }

    override suspend fun updateLearningProgress(): Boolean {
        val userId = db.requireCurrentUserId()
        val learningProgress = getUserLearningProgress()

        val currentLessonId = learningProgress.lessonId ?: return false
        val currentModuleId = learningProgress.moduleId
            ?: error("Module id is null while lesson id is not null")

        val sortedModules = db.modules.sortedBy { it.moduleId }
        val currentModuleIndex = sortedModules.indexOfFirst { it.moduleId == currentModuleId }
        if (currentModuleIndex == -1) {
            error("Module not found: $currentModuleId")
        }

        val currentModule = sortedModules[currentModuleIndex]
        val lessonIds = getLessonIdsForModule(currentModule.moduleId)

        if (lessonIds.isEmpty()) {
            return advanceToNextModuleFirstLesson(
                userId,
                learningProgress,
                sortedModules,
                currentModuleIndex
            )
        }

        val currentLessonIdx = lessonIds.indexOf(currentLessonId)
        if (currentLessonIdx == -1) {
            error("Invalid lesson id $currentLessonId for module $currentModuleId")
        }

        val nextLessonIdx = currentLessonIdx + 1
        if (nextLessonIdx in lessonIds.indices) {
            db.putUserLearningProgress(
                userId,
                learningProgress.copy(lessonId = lessonIds[nextLessonIdx])
            )
            return true
        }

        return advanceToNextModuleFirstLesson(
            userId,
            learningProgress,
            sortedModules,
            currentModuleIndex
        )
    }

    override suspend fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        val userId = db.requireCurrentUserId()
        val summary = db.getProgressSummary(userId) ?: error("Progress summary not found")

        val updated = summary.copy(
            dailyProgress = summary.dailyProgress.copy(dailyGoalMinutes = minutesPerDay),
            weeklyProgress = summary.weeklyProgress.copy(weeklyGoalDays = daysPerWeek)
        )
        db.putProgressSummary(userId, updated)
    }

    override suspend fun completeLesson(lessonId: Long): Boolean {
        val userId = db.requireCurrentUserId()
        val progress = db.getUserLearningProgress(userId)
            ?: error("User learning progress not found")

        val inserted = db.addCompletedLesson(userId, lessonId)
        if (!inserted) return false

        val wordsInLesson = db.signs.count { it.lessonId == lessonId }
        db.putUserLearningProgress(
            userId,
            progress.copy(wordsLearned = progress.wordsLearned + wordsInLesson)
        )
        return true
    }

    override suspend fun getCompletedLessonIds(): Set<Long> {
        val userId = db.requireCurrentUserId()
        return db.getCompletedLessonIdsForUser(userId)
    }

    private fun getLessonIdsForModule(moduleId: Long): List<Long> {
        return db.lessons
            .filter { it.moduleId == moduleId }
            .sortedBy { it.lessonId }
            .map { it.lessonId }
    }

    private fun advanceToNextModuleFirstLesson(
        userId: String,
        learningProgress: UserLearningProgress,
        sortedModules: List<Module>,
        currentModuleIndex: Int
    ): Boolean {
        val nextModuleIndex = currentModuleIndex + 1

        if (nextModuleIndex > sortedModules.lastIndex) {
            db.putUserLearningProgress(
                userId,
                learningProgress.copy(
                    moduleId = null,
                    lessonId = null
                )
            )
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

        db.putUserLearningProgress(
            userId,
            learningProgress.copy(
                moduleId = nextModule.moduleId,
                lessonId = nextLessonIds.first()
            )
        )
        return true
    }
}