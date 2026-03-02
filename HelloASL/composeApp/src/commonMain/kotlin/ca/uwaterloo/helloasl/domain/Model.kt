package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.repository.AuthRepository
import ca.uwaterloo.helloasl.data.repository.UserRepository
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

data class Repositories(
    val auth: AuthRepository,
    val user: UserRepository,
)
class Model (private val repos: Repositories) {
    fun getUser(): User {
        return repos.user.getUser()
    }
    fun getUserProfile(): UserProfile {
        return repos.user.getUserProfile()
    }
    fun setLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        repos.user.updateLearningGoals(minutesPerDay, daysPerWeek)
    }
    fun signup(name: String, email: String, password: String): Boolean {
        return repos.auth.signup(name, email, password)
    }
    fun login(email: String, password: String): Boolean {
        return repos.auth.login(email, password)
    }
    fun logout() {
        repos.auth.logout()
    }
}