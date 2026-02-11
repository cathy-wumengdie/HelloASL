package ca.uwaterloo.helloasl.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

data class LoginUiState(
    val email: String = "yanjin@email.com",   // ✅ hard-coded test data
    val password: String = "123456",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel {

    private val _uiState = mutableStateOf(LoginUiState())
    val uiState: State<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onSignIn(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        // fake auth logic for sprint demo
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Email and password cannot be empty"
            )
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        }
    }
}
