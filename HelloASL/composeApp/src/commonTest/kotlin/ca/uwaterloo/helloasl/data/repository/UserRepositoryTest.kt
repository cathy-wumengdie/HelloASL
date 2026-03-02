package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.data.MockDB
import kotlin.test.*

class UserRepositoryTest {
    private fun makeRepo(): Pair<MockDB, UserRepository> {
        val db = MockDB()
        val repo: UserRepository = MockUserRepository(db)
        return db to repo
    }

    @Test
    fun getUser_throws_when_not_logged_in() {
        val (_, repo) = makeRepo()
        assertFailsWith<IllegalStateException> {
            repo.getUser()
        }
    }

    @Test
    fun getUser_returns_logged_in_user() {
        val (db, repo) = makeRepo()
        val ok = db.login(email = "yanjin@gmail.com", password = "1234")
        assertEquals(true, ok)

        val user = repo.getUser()
        assertEquals(1, user.id)
        assertEquals("Yanjin", user.name)
        assertEquals("yanjin@gmail.com", user.email)
    }

    @Test
    fun getUserProfile_returns_profile_for_logged_in_user() {
        val (db, repo) = makeRepo()
        db.login("yanjin@gmail.com", "1234")

        val profile = repo.getUserProfile()
        assertEquals(1, profile.userId)
        assertEquals(15, profile.learningGoalPerDay)
        assertEquals(3, profile.learningGoalPerWeek)
        assertEquals(7, profile.streakDays)
    }

    @Test
    fun updateLearningGoals_updates_profile_values() {
        val (db, repo) = makeRepo()
        db.login("yanjin@gmail.com", "1234")
        repo.updateLearningGoals(minutesPerDay = 20, daysPerWeek = 5)

        val updated = repo.getUserProfile()
        assertEquals(20, updated.learningGoalPerDay)
        assertEquals(5, updated.learningGoalPerWeek)
    }

    @Test
    fun updateLearningGoals_throws_when_not_logged_in() {
        val (_, repo) = makeRepo()
        assertFailsWith<IllegalStateException> {
            repo.updateLearningGoals(20, 5)
        }
    }
}