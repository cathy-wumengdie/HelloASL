package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.userModel.User
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository

class UserModelTest {
    private fun makeModel(): Pair<MockDB, Model> {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db),
            notification = NoOpNotificationRepository
        )
        return db to Model(repos)
    }

    @Test
    fun getUser_throws_when_not_logged_in() {
        val (_, model) = makeModel()

        runBlocking {
            assertFailsWith<IllegalStateException> {
                model.getUser()
            }
        }
    }

    @Test
    fun getUser_returns_logged_in_user() {
        val (_, model) = makeModel()

        runBlocking {
            model.login(email = "yanjin@gmail.com", password = "1234")

            val user = model.getUser()
            assertEquals("1", user.id)
            assertEquals("Yanjin", user.name)
            assertEquals("yanjin@gmail.com", user.email)
        }
    }

    @Test
    fun getUserLearningProgress_returns_progress_for_logged_in_user() {
        val (_, model) = makeModel()

        runBlocking {
            model.login("yanjin@gmail.com", "1234")

            val learningProgress = model.getUserLearningProgress()
            assertEquals("1", learningProgress.userId)
            assertEquals(1L, learningProgress.moduleId)
            assertEquals(1L, learningProgress.lessonId)
            assertEquals(0, learningProgress.wordsLearned)
            assertFalse(learningProgress.completedAllLessons)
        }
    }

    @Test
    fun getProgressSummary_returns_summary_for_logged_in_user() {
        val (_, model) = makeModel()

        runBlocking {
            model.login("yanjin@gmail.com", "1234")

            val summary = model.getProgressSummary()
            assertEquals("1", summary.userId)
            assertEquals(15, summary.dailyProgress.dailyGoalMinutes)
            assertEquals(3, summary.weeklyProgress.weeklyGoalDays)
            assertEquals(7, summary.dayStreak)
        }
    }

    @Test
    fun setLearningGoals_updates_progress_summary_values() {
        val (_, model) = makeModel()

        runBlocking {
            model.login("yanjin@gmail.com", "1234")
            model.setLearningGoals(minutesPerDay = 20, daysPerWeek = 5)

            val updated = model.getProgressSummary()
            assertEquals(20, updated.dailyProgress.dailyGoalMinutes)
            assertEquals(5, updated.weeklyProgress.weeklyGoalDays)
        }
    }

    @Test
    fun setLearningGoals_throws_when_not_logged_in() {
        val (_, model) = makeModel()

        runBlocking {
            assertFailsWith<IllegalStateException> {
                model.setLearningGoals(20, 5)
            }
        }
    }

    @Test
    fun avatarText_single_name_returns_first_letter_uppercase() {
        val user = User(
            id = "1",
            name = "yanjin",
            email = "yanjin@gmail.com"
        )
        assertEquals("Y", user.avatarText)
    }

    @Test
    fun avatarText_single_letter_name() {
        val user = User(
            id = "1",
            name = "a",
            email = "a@gmail.com"
        )
        assertEquals("A", user.avatarText)
    }

    @Test
    fun avatarText_empty_name_returns_empty_string() {
        val user = User(
            id = "1",
            name = "   ",
            email = "test@gmail.com"
        )
        assertEquals("", user.avatarText)
    }

    @Test
    fun avatarText_three_word_name_uses_first_and_last_handles_extra_space() {
        val user = User(
            id = "1",
            name = "Yanjin    Mei    Xia",
            email = "yanjin@gmail.com"
        )
        assertEquals("YX", user.avatarText)
    }

    @Test
    fun isLoggedIn_becomes_true_after_login() {
        val (_, model) = makeModel()

        runBlocking {
            assertFalse(model.isLoggedIn())

            model.login("yanjin@gmail.com", "1234")
            assertTrue(model.isLoggedIn())
        }
    }
}