package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

interface UserRepository {
    fun getUser(): User
    fun getUserProfile(): UserProfile
    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int)
}