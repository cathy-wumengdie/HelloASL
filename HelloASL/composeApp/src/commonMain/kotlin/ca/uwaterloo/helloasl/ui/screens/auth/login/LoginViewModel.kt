package ca.uwaterloo.helloasl.ui.screens.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class LoginViewModel {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun onSignIn() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        // TODO: auth logic
    }
}
