package ca.uwaterloo.helloasl.ui.screens.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ca.uwaterloo.helloasl.data.authRepository.SignUpResult
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.ui.utils.AuthErrorMapper.friendlyMessage

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val verificationEmailSent: Boolean = false
)

class SignupViewModel(private val model: Model) {

    private val _uiState = mutableStateOf(SignupUiState())
    val uiState: State<SignupUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun resetState() {
        _uiState.value = SignupUiState()
    }

    suspend fun onCreateAccount(
        onSuccess: () -> Unit,
        onNeedsVerification: (String) -> Unit
    ) {
        val state = _uiState.value
        val name = state.name.trim()
        val email = state.email.trim()
        val password = state.password
        val confirm = state.confirmPassword

        if (name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Name cannot be empty", infoMessage = null)
            return
        }
        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email cannot be empty", infoMessage = null)
            return
        }
        if (password.isBlank() || confirm.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Password cannot be empty", infoMessage = null)
            return
        }
        if (password != confirm) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match", infoMessage = null)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, infoMessage = null)

        when (val result = model.signup(name, email, password)) {
            SignUpResult.Success -> {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }

            SignUpResult.NeedsEmailVerification -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    infoMessage = "Account created. Please verify your email before logging in.",
                    verificationEmailSent = true
                )
                onNeedsVerification(email)
            }

            is SignUpResult.Failure -> {
                println("SIGNUP ERROR: ${result.error::class.qualifiedName}")
                println("SIGNUP ERROR MESSAGE: ${result.error.message}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = friendlyMessage(result.error),
                    infoMessage = null
                )
            }
        }
    }
}