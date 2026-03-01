package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserCredential
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import ca.uwaterloo.helloasl.domain.userModel.UserSession
import java.util.Objects.hash

object MockDB {
    private val users = mutableMapOf(
        1 to User(id = 1, name = "Yanjin", email = "yanjin@gmail.com")
    )

    private val credentials = mutableMapOf(
        1 to UserCredential(userId = 1, passwordHash = hash("1234")) // default password
    )

    var userSession: UserSession? = null

    private var userProfiles = mutableMapOf(
        1 to UserProfile(
            userId = 1,
            learningGoalPerDay = 15,
            learningGoalPerWeek = 3,
            learningProgress = LearningProgress(module = 2, lesson = 3),
            wordsLearned = 40,
            starredSigns = 12,
            streakDays = 7
        )
    )

    private fun getUserId(): Int {
        return userSession?.userId ?: throw IllegalStateException("User not logged in")
    }

    fun getUser(): User {
        val userId = getUserId()
        return users[userId] ?: error("User not found")
    }

    fun getUserProfile(): UserProfile {
        val userId = getUserId()
        return userProfiles[userId] ?: error("Profile not found")
    }

    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        val userId = getUserId()
        val oldProfile = userProfiles[userId] ?: error("Profile not found")
        userProfiles[userId] = oldProfile.copy(
            learningGoalPerDay = minutesPerDay,
            learningGoalPerWeek = daysPerWeek
        )
    }

    fun signup(name: String, email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()

        // reject duplicate emails
        if (users.values.any { it.email.lowercase() == cleanEmail }) return false

        val newUserId = users.size
        val newUser = User(id = newUserId, name = name.trim(), email = cleanEmail)
        users[newUserId] = newUser
        credentials[newUserId] = UserCredential(newUser.id, hash(password))
        userProfiles[newUserId] = UserProfile(
            userId = newUserId,
            learningGoalPerDay = 0,
            learningGoalPerWeek = 0,
            learningProgress = LearningProgress(module = 1, lesson = 1),
            wordsLearned = 0,
            starredSigns = 0,
            streakDays = 0
        )

        // auto-login after signup
        userSession = UserSession(
            userId = newUser.id,
            userName = name,
            email = email,
            loginTime = System.currentTimeMillis()
        )
        return true
    }

    fun login(email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val user = users.values.find { it.email.lowercase() == cleanEmail } ?: return false
        val cred = credentials[user.id] ?: return false

        if (cred.passwordHash != hash(password)) return false

        userSession = UserSession(
            userId = user.id,
            userName = user.name,
            email = user.email,
            loginTime = System.currentTimeMillis()
        )
        return true
    }

    fun logout() {
        userSession = null
    }
}