package ca.uwaterloo.helloasl.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ca.uwaterloo.helloasl.domain.Model

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
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onSignIn(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val email = _uiState.value.email
        val password = _uiState.value.password

        // fake auth logic for sprint demo
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Email and password cannot be empty"
            )
        }
        val ok = model.login(email, password)

        if (ok) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Invalid email or password"
            )
        }
    }
}
