package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile

class MockUserRepository(private val db: MockDB): UserRepository {
    override fun getUser(): User {
        return db.getUser()
    }

    override fun getUserProfile(): UserProfile {
        return db.getUserProfile()
    }

    override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        return db.updateLearningGoals(minutesPerDay, daysPerWeek)
    }
}