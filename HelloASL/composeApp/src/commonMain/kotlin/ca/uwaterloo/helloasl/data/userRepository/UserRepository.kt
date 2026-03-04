package ca.uwaterloo.helloasl.data.userRepository

import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

interface UserRepository {
    fun getUser(): User
    fun getUserProfile(): UserProfile
    fun updateLearningProgress(moduleId: Int, lessonId: Int)
    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int)
    fun updateWordsLearned(wordsLearned: Int)
}