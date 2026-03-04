package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelAuthTest {
    private fun makeModel(): Pair<MockDB, Model> {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        return db to Model(repos)
    }

    @Test
    fun login_success_sets_session() {
        val (db, model) = makeModel()
        val ok = model.login(email = "yanjin@gmail.com", password = "1234")

        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals(1, db.userSession!!.userId)
    }

    @Test
    fun login_fail_wrong_password_does_not_set_session() {
        val (db, model) = makeModel()
        val ok = model.login(email = "yanjin@gmail.com", password = "wrong")

        assertFalse(ok)
        assertNull(db.userSession)
    }

    @Test
    fun login_fail_unknown_email_does_not_set_session() {
        val (db, model) = makeModel()
        val ok = model.login(email = "noone@gmail.com", password = "1234")

        assertFalse(ok)
        assertNull(db.userSession)
    }

    @Test
    fun logout_clears_session() {
        val (db, model) = makeModel()

        model.login("yanjin@gmail.com", "1234")
        assertNotNull(db.userSession)

        model.logout()
        assertNull(db.userSession)
    }

    @Test
    fun signup_success_creates_user_and_logs_in() {
        val (db, model) = makeModel()
        val ok = model.signup(name = "Alice", email = "alice@gmail.com", password = "pw")

        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals("Alice", db.userSession!!.userName)
        assertEquals("alice@gmail.com", db.userSession!!.email)
    }

    @Test
    fun signup_assigns_unique_user_id() {
        val (db, model) = makeModel()
        val existingIds = db.getAllUserIds()

        val ok = model.signup(name = "Alice", email = "alice@gmail.com", password = "pw")
        assertTrue(ok)

        val newUserId = db.userSession!!.userId
        assertFalse(existingIds.contains(newUserId))
    }

    @Test
    fun signup_fail_duplicate_email_returns_false_and_does_not_change_session() {
        val (db, model) = makeModel()

        val ok1 = model.signup("Erdo", "erdo@gmail.com", "pw")
        assertTrue(ok1)
        val firstSessionUserId = db.userSession!!.userId

        model.logout()
        assertNull(db.userSession)

        val ok2 = model.signup("David", "erdo@gmail.com", "pw2")
        assertFalse(ok2)
        assertNull(db.userSession)

        val okLogin = model.login("erdo@gmail.com", "pw")
        assertTrue(okLogin)
        assertEquals(firstSessionUserId, db.userSession!!.userId)
    }

    @Test
    fun login_trims_and_lowercases_email() {
        val (db, model) = makeModel()
        val ok = model.login(email = "  YANJIN@GMAIL.COM  ", password = "1234")

        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals(1, db.userSession!!.userId)
    }
}