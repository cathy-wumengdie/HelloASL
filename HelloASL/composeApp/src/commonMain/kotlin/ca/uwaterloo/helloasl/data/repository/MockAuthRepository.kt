package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.data.MockDB

class MockAuthRepository (private val db: MockDB): AuthRepository {
    override fun signup(name: String, email: String, password: String): Boolean = db.signup(name, email, password)
    override fun login(email: String, password: String): Boolean = db.login(email, password)
    override fun logout() {
        return db.logout()
    }
}