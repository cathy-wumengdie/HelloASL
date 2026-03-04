package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import kotlin.test.*

class UserModelTest {
    private fun makeModel(): Pair<MockDB, Model> {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            learning = MockLearningRepository(db),
            progressTracker = MockProgressTrackerRepository(db),
        )
        return db to Model(repos)
    }

    @Test
    fun getUser_throws_when_not_logged_in() {
        val (_, model) = makeModel()
        assertFailsWith<IllegalStateException> {
            model.getUser()
        }
    }

    @Test
    fun getUser_returns_logged_in_user() {
        val (_, model) = makeModel()
        val ok = model.login(email = "yanjin@gmail.com", password = "1234")
        assertEquals(true, ok)

        val user = model.getUser()
        assertEquals(1, user.id)
        assertEquals("Yanjin", user.name)
        assertEquals("yanjin@gmail.com", user.email)
    }

    @Test
    fun getUserProfile_returns_profile_for_logged_in_user() {
        val (_, model) = makeModel()
        model.login("yanjin@gmail.com", "1234")

        val profile = model.getUserProfile()
        assertEquals(1, profile.userId)
        assertEquals(15, profile.progressSummary.dailyProgress.dailyGoalMinutes)
        assertEquals(3, profile.progressSummary.weeklyProgress.weeklyGoalDays)
        assertEquals(7, profile.progressSummary.dayStreak)
    }

    @Test
    fun updateLearningGoals_updates_profile_values() {
        val (_, model) = makeModel()
        model.login("yanjin@gmail.com", "1234")

        model.setLearningGoals(minutesPerDay = 20, daysPerWeek = 5)

        val updated = model.getUserProfile()
        assertEquals(20, updated.progressSummary.dailyProgress.dailyGoalMinutes)
        assertEquals(5, updated.progressSummary.weeklyProgress.weeklyGoalDays)
    }

    @Test
    fun updateLearningGoals_throws_when_not_logged_in() {
        val (_, model) = makeModel()
        assertFailsWith<IllegalStateException> {
            model.setLearningGoals(20, 5)
        }
    }

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
        val (db, model) = makeModel()
        val ok = db.login("yanjin@gmail.com", "1234")
        assertTrue(ok)

        // Put user at Module 1, Lesson 3 so finished Lesson 1 and 2
        // Should include signs from lessons 1 and 2 => 4 signs total.
        db.updateLearningProgress(moduleId = 1, lessonId = 3)
        assertEquals(4, model.getNumberOfWordsLearned())
    }
}