package ca.uwaterloo.helloasl.data

import kotlin.test.*

class MockDBTest {

    @Test
    fun getUser_throws_when_not_logged_in() {
        val db = MockDB()
        assertFailsWith<IllegalStateException> {
            db.getUser()
        }
    }

    @Test
    fun login_success_sets_session_and_allows_getUser() {
        val db = MockDB()
        val ok = db.login("yanjin@gmail.com", "1234")
        assertTrue(ok)

        assertNotNull(db.userSession)
        assertEquals(1, db.userSession!!.userId)

        val user = db.getUser()
        assertEquals(1, user.id)
        assertEquals("Yanjin", user.name)
        assertEquals("yanjin@gmail.com", user.email)
    }

    @Test
    fun login_fails_with_wrong_password_and_session_stays_null() {
        val db = MockDB()
        val ok = db.login("yanjin@gmail.com", "wrong")
        assertFalse(ok)
        assertNull(db.userSession)
    }

    @Test
    fun login_trims_and_lowercases_email() {
        val db = MockDB()
        val ok = db.login("  YANJIN@GMAIL.COM  ", "1234")
        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals(1, db.userSession!!.userId)
    }

    @Test
    fun logout_clears_session() {
        val db = MockDB()
        db.login("yanjin@gmail.com", "1234")
        assertNotNull(db.userSession)

        db.logout()
        assertNull(db.userSession)
    }

    @Test
    fun getUserProfile_returns_profile_after_login() {
        val db = MockDB()
        db.login("yanjin@gmail.com", "1234")

        val profile = db.getUserProfile()
        assertEquals(1, profile.userId)
        assertEquals(15, profile.learningGoalPerDay)
        assertEquals(3, profile.learningGoalPerWeek)
        assertEquals(7, profile.streakDays)
    }

    @Test
    fun updateLearningGoals_updates_profile_fields() {
        val db = MockDB()
        db.login("yanjin@gmail.com", "1234")
        db.updateLearningGoals(minutesPerDay = 20, daysPerWeek = 5)

        val updated = db.getUserProfile()
        assertEquals(20, updated.learningGoalPerDay)
        assertEquals(5, updated.learningGoalPerWeek)
    }

    @Test
    fun signup_success_auto_logs_in_and_then_login_with_same_password_works() {
        val db = MockDB()
        val okSignup = db.signup("Alice", "alice@gmail.com", "pw")
        assertTrue(okSignup)
        assertNotNull(db.userSession)
        assertEquals("Alice", db.userSession!!.userName)

        db.logout()
        assertNull(db.userSession)

        val okLogin = db.login("alice@gmail.com", "pw")
        assertTrue(okLogin)
        assertNotNull(db.userSession)
    }

    @Test
    fun signup_assigns_unique_user_id() {
        val db = MockDB()
        // collect existing user ids
        val existingIds = db.getAllUserIds()
        val ok = db.signup(
            name = "Alice",
            email = "alice@gmail.com",
            password = "pw"
        )
        assertTrue(ok)

        val newUserId = db.userSession!!.userId
        // verify new id does not already exist
        assertFalse(existingIds.contains(newUserId))
    }

    @Test
    fun signup_rejects_duplicate_email_case_insensitive() {
        val db = MockDB()
        val ok1 = db.signup("Alice", "alice@gmail.com", "pw")
        assertTrue(ok1)

        db.logout()

        val ok2 = db.signup("Bob", "  ALICE@GMAIL.COM  ", "pw2")
        assertFalse(ok2)
        assertNull(db.userSession) // should not auto-login on failed signup
    }
}