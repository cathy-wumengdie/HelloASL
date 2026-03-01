package ca.uwaterloo.helloasl.ui.screens.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ca.uwaterloo.helloasl.domain.Model

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignupViewModel (private val model: Model) {

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

    fun onCreateAccount(onSuccess: () -> Unit) {
        val state = _uiState.value
        val name = state.name.trim()
        val email = state.email.trim()
        val password = state.password
        val confirm = state.confirmPassword

        if (name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Name cannot be empty")
            return
        }
        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email cannot be empty")
            return
        }
        if (password.isBlank() || confirm.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Password cannot be empty")
            return
        }
        if (password != confirm) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        val ok = model.signup(name, email, password)
        _uiState.value = _uiState.value.copy(isLoading = false)

        if (ok) onSuccess()
        else _uiState.value = _uiState.value.copy(errorMessage = "Email already exists")
    }
}
