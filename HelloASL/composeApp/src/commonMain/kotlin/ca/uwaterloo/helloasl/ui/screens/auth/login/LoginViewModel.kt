package ca.uwaterloo.helloasl.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ca.uwaterloo.helloasl.data.authRepository.LoginResult
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.ui.utils.AuthErrorMapper.friendlyMessage

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val model: Model) {
    private val _uiState = mutableStateOf(LoginUiState())
    val uiState: State<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    suspend fun onSignIn(onSuccess: () -> Unit) {
        val email = _uiState.value.email
        val password = _uiState.value.password

        // fake auth logic for sprint demo
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Email and password cannot be empty"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        when (val result = model.login(email, password)) {
            LoginResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
                onSuccess()
            }
            LoginResult.EmailNotVerified -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Please verify your email before signing in."
                )
            }
            is LoginResult.Failure -> {
                println("LOGIN ERROR: ${result.error::class.qualifiedName}")
                println("LOGIN ERROR MESSAGE: ${result.error.message}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = friendlyMessage(result.error)
                )
            }
        }
    }
}
