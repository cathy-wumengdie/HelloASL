package ca.uwaterloo.helloasl.ui.screens.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

data class SignupUiState(
    val email: String = "test@email.com",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null
)

class SignupViewModel {

    private val _uiState = mutableStateOf(SignupUiState())
    val uiState: State<SignupUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun onCreateAccount(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.password.isBlank() || state.confirmPassword.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Password cannot be empty"
            )
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }

        // fake success for sprint demo
        _uiState.value = state.copy(errorMessage = null)
        onSuccess()
    }
}
