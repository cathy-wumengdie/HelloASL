package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.repository.HelloASLRepository
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

class Model (private val repo: HelloASLRepository) {
    fun getUser(): User {
        return repo.getUser()
    }
    fun getUserProfile(): UserProfile {
        return repo.getUserProfile()
    }
    fun setLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        repo.updateLearningGoals(minutesPerDay, daysPerWeek)
    }
    fun signup(name: String, email: String, password: String): Boolean {
        return repo.signup(name, email, password)
    }
    fun login(email: String, password: String): Boolean {
        return repo.login(email, password)
    }
    fun logout() {
        repo.logout()
    }
}