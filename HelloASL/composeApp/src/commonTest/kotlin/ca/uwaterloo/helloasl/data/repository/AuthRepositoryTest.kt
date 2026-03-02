package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.data.MockDB
import kotlin.test.*

class AuthRepositoryTest {
    private fun makeRepo(): Pair<MockDB, AuthRepository> {
        val db = MockDB()
        val repo: AuthRepository = MockAuthRepository(db)
        return db to repo
    }

    @Test
    fun login_success_sets_session() {
        val (db, repo) = makeRepo()
        val ok = repo.login(email = "yanjin@gmail.com", password = "1234")

        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals(1, db.userSession!!.userId)
    }

    @Test
    fun login_fail_wrong_password_does_not_set_session() {
        val (db, repo) = makeRepo()
        val ok = repo.login(email = "yanjin@gmail.com", password = "wrong")

        assertFalse(ok)
        assertNull(db.userSession)
    }

    @Test
    fun login_fail_unknown_email_does_not_set_session() {
        val (db, repo) = makeRepo()
        val ok = repo.login(email = "noone@gmail.com", password = "1234")

        assertFalse(ok)
        assertNull(db.userSession)
    }

    @Test
    fun logout_clears_session() {
        val (db, repo) = makeRepo()

        repo.login("yanjin@gmail.com", "1234")
        assertNotNull(db.userSession)

        repo.logout()
        assertNull(db.userSession)
    }

    @Test
    fun signup_success_creates_user_and_logs_in() {
        val (db, repo) = makeRepo()
        val ok = repo.signup(name = "Alice", email = "alice@gmail.com", password = "pw")

        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals("Alice", db.userSession!!.userName)
        assertEquals("alice@gmail.com", db.userSession!!.email)
    }

    @Test
    fun signup_assigns_unique_user_id() {
        val (db, repo) = makeRepo()
        // collect existing user ids
        val existingIds = db.getAllUserIds()
        val ok = repo.signup(
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
    fun signup_fail_duplicate_email_returns_false_and_does_not_change_session() {
        val (db, repo) = makeRepo()

        val ok1 = repo.signup("Erdo", "erdo@gmail.com", "pw")
        assertTrue(ok1)
        val firstSessionUserId = db.userSession!!.userId

        repo.logout()
        assertNull(db.userSession)

        // Second signup with same email should fail
        val ok2 = repo.signup("David", "erdo@gmail.com", "pw2")
        assertFalse(ok2)
        assertNull(db.userSession)

        // login with Erdo should still work
        val okLogin = repo.login("erdo@gmail.com", "pw")
        assertTrue(okLogin)
        assertEquals(firstSessionUserId, db.userSession!!.userId)
    }

    @Test
    fun login_trims_and_lowercases_email() {
        val (db, repo) = makeRepo()
        val ok = repo.login(email = "  YANJIN@GMAIL.COM  ", password = "1234")

        assertTrue(ok)
        assertNotNull(db.userSession)
        assertEquals(1, db.userSession!!.userId)
    }
}