package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

interface HelloASLRepository {
    fun getUser(): User

    // profile page
    fun getUserProfile(): UserProfile
    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int)

    // authentication
    fun signup(name: String, email: String, password: String): Boolean
    fun login(email: String, password: String): Boolean
    fun logout()
}