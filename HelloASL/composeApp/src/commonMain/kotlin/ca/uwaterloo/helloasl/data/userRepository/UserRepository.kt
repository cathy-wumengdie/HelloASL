package ca.uwaterloo.helloasl.data.userRepository

import ca.uwaterloo.helloasl.domain.userModel.*

interface UserRepository {
    suspend fun getUser(): User
    suspend fun getUserLearningProgress(): UserLearningProgress
    suspend fun updateLearningProgress(): Boolean
    suspend fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int)
    suspend fun completeLesson(lessonId: Long): Boolean
    suspend fun getCompletedLessonIds(): Set<Long>
}