package ca.uwaterloo.helloasl.data.userRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.userModel.*
import ca.uwaterloo.helloasl.domain.starModel.StarItem

class MockUserRepository(private val db: MockDB): UserRepository {
    override fun getUser(): User {
        return db.getUser()
    }

    override fun getUserLearningProgress(): UserLearningProgress {
        return db.getUserLearningProgress()
    }

    override fun updateLearningProgress(): Boolean {
        return db.updateLearningProgress()
    }

    override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        return db.updateLearningGoals(minutesPerDay, daysPerWeek)
    }
    override fun updateWordsLearned(wordsLearned: Int) {
        db.updateWordsLearned(wordsLearned)
    }

    override fun getStarredItems(): List<StarItem> = db.getStarredItems()

    override fun removeStar(itemId: String) {
        db.removeStar(itemId)
    }
}