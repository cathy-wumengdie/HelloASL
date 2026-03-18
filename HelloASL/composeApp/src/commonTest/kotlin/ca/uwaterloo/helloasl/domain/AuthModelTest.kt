package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import kotlinx.coroutines.runBlocking
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
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        return db to Model(repos)
    }

    @Test
    fun isLoggedIn_initially_false() {
        val (_, model) = makeModel()
        assertFalse(model.isLoggedIn())
    }

    @Test
    fun login_success_sets_session() = runBlocking {
        val (db, model) = makeModel()

        model.login(email = "yanjin@gmail.com", password = "1234")

        val session = db.getUserSession()
        assertNotNull(session)
        assertEquals("1", session.userId)
        assertEquals("Yanjin", session.userName)
        assertEquals("yanjin@gmail.com", session.email)
        assertTrue(model.isLoggedIn())
    }

    @Test
    fun login_fail_wrong_password_does_not_set_session() = runBlocking {
        val (db, model) = makeModel()

        model.login(email = "yanjin@gmail.com", password = "wrong")

        assertNull(db.getUserSession())
        assertFalse(model.isLoggedIn())
    }

    @Test
    fun login_fail_unknown_email_does_not_set_session() = runBlocking {
        val (db, model) = makeModel()

        model.login(email = "noone@gmail.com", password = "1234")

        assertNull(db.getUserSession())
        assertFalse(model.isLoggedIn())
    }

    @Test
    fun login_trims_and_lowercases_email() = runBlocking {
        val (db, model) = makeModel()

        model.login(email = "  YANJIN@GMAIL.COM  ", password = "1234")

        val session = db.getUserSession()
        assertNotNull(session)
        assertEquals("1", session.userId)
        assertTrue(model.isLoggedIn())
    }

    @Test
    fun logout_clears_session() = runBlocking {
        val (db, model) = makeModel()

        model.login("yanjin@gmail.com", "1234")
        assertNotNull(db.getUserSession())
        assertTrue(model.isLoggedIn())

        model.logout()

        assertNull(db.getUserSession())
        assertFalse(model.isLoggedIn())
    }

    @Test
    fun signup_success_creates_user_and_logs_in() = runBlocking {
        val (db, model) = makeModel()

        model.signup(name = "Alice", email = "alice@gmail.com", password = "pw")

        val session = db.getUserSession()
        assertNotNull(session)
        assertEquals("Alice", session.userName)
        assertEquals("alice@gmail.com", session.email)
        assertTrue(model.isLoggedIn())
    }

    @Test
    fun signup_assigns_unique_user_id() = runBlocking {
        val (db, model) = makeModel()
        val existingIds = db.getAllUserIds()

        model.signup(name = "Alice", email = "alice@gmail.com", password = "pw")

        val session = db.getUserSession()
        assertNotNull(session)
        assertFalse(existingIds.contains(session.userId))
    }

    @Test
    fun signup_fail_duplicate_email_does_not_create_new_session() = runBlocking {
        val (db, model) = makeModel()

        model.signup("Erdo", "erdo@gmail.com", "pw")
        val firstSession = db.getUserSession()
        assertNotNull(firstSession)
        val firstSessionUserId = firstSession.userId

        model.logout()
        assertNull(db.getUserSession())
        assertFalse(model.isLoggedIn())

        model.signup("David", "erdo@gmail.com", "pw2")

        assertNull(db.getUserSession())
        assertFalse(model.isLoggedIn())

        model.login("erdo@gmail.com", "pw")
        val loginSession = db.getUserSession()
        assertNotNull(loginSession)
        assertEquals(firstSessionUserId, loginSession.userId)
    }

    @Test
    fun signup_then_logout_updates_logged_in_state() = runBlocking {
        val (_, model) = makeModel()

        assertFalse(model.isLoggedIn())

        model.signup("Alice", "alice@gmail.com", "pw")
        assertTrue(model.isLoggedIn())

        model.logout()
        assertFalse(model.isLoggedIn())
    }
}