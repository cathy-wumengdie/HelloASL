package ca.uwaterloo.helloasl.data.userRepository

import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import ca.uwaterloo.helloasl.domain.starModel.StarItem

interface UserRepository {
    fun getUser(): User
    fun getUserProfile(): UserProfile
    fun updateLearningProgress(): Boolean
    fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int)
    fun updateWordsLearned(wordsLearned: Int)
    fun getStarredItems(): List<StarItem>
    fun removeStar(itemId: String)
}