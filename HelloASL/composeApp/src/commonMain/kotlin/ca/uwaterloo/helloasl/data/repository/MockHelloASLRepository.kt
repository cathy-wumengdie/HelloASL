package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

class MockHelloASLRepository : HelloASLRepository {
    override fun getUser(): User {
        return MockDB.getUser()
    }

    // profile page
    override fun getUserProfile(): UserProfile {
        return MockDB.getUserProfile()
    }

    override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        return MockDB.updateLearningGoals(minutesPerDay, daysPerWeek)
    }

    // authentication
    override fun signup(name: String, email: String, password: String): Boolean = MockDB.signup(name, email, password)
    override fun login(email: String, password: String): Boolean = MockDB.login(email, password)
    override fun logout() {
        return MockDB.logout()
    }
}