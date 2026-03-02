package ca.uwaterloo.helloasl.data.repository

interface AuthRepository {
    fun signup(name: String, email: String, password: String): Boolean
    fun login(email: String, password: String): Boolean
    fun logout()
}