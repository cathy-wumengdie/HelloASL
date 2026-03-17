package ca.uwaterloo.helloasl.data.authRepository

sealed interface SignUpResult {
    data object Success : SignUpResult
    data object NeedsEmailVerification : SignUpResult
    data class Failure(val error: Throwable) : SignUpResult
}

sealed interface LoginResult {
    data object Success : LoginResult
    data object EmailNotVerified : LoginResult
    data class Failure(val error: Throwable) : LoginResult
}

interface AuthRepository {
    suspend fun signup(name: String, email: String, password: String): SignUpResult
    suspend fun login(email: String, password: String): LoginResult
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUserId(): String?
    fun isLoggedIn(): Boolean
}