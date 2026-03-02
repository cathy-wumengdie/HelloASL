package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import kotlin.test.*

class UserModelTest {
    @Test
    fun avatarText_single_name_returns_first_letter_uppercase() {
        val user = User(
            id = 1,
            name = "yanjin",
            email = "yanjin@gmail.com"
        )
        assertEquals("Y", user.avatarText)
    }

    @Test
    fun avatarText_single_letter_name() {
        val user = User(
            id = 1,
            name = "a",
            email = "a@gmail.com"
        )
        assertEquals("A", user.avatarText)
    }

    @Test
    fun avatarText_empty_name_returns_empty_string() {
        val user = User(
            id = 1,
            name = "   ",
            email = "test@gmail.com"
        )
        assertEquals("", user.avatarText)
    }

    @Test
    fun avatarText_three_word_name_uses_first_and_last_handles_extra_space() {
        val user = User(
            id = 1,
            name = "Yanjin    Mei    Xia",
            email = "yanjin@gmail.com"
        )
        assertEquals("YX", user.avatarText)
    }

    @Test
    fun getNumberOfWordsLearned_return() {
        /* update once getNumberOfWordsLearned completed */
        val profile = UserProfile(
            userId = 1,
            learningGoalPerDay = 15,
            learningGoalPerWeek = 3,
            streakDays = 7,
            learningProgress = LearningProgress(module = 2, lesson = 3),
            wordsLearned = 40,
            starredSigns = 12
        )
        assertEquals(40, profile.getNumberOfWordsLearned())
    }
}