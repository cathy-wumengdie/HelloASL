package ca.uwaterloo.helloasl.data.authRepository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthRepository(
    private val supabase: SupabaseClient
) : AuthRepository {
    override suspend fun signup(name: String, email: String, password: String): SignUpResult {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                }
            }

            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                SignUpResult.NeedsEmailVerification
            } else {
                SignUpResult.Success
            }
        } catch (e: Throwable) {
            SignUpResult.Failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): LoginResult {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val hasSession = supabase.auth.currentSessionOrNull() != null
            if (hasSession) {
                LoginResult.Success
            } else {
                LoginResult.Failure(
                    IllegalStateException("No session created after login")
                )
            }
        } catch (e: Throwable) {
            LoginResult.Failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabase.auth.signOut()
    }

    override suspend fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    override fun isLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    override suspend fun updatePassword(newPassword: String) {
        supabase.auth.updateUser {
            password = newPassword
        }
    }
}