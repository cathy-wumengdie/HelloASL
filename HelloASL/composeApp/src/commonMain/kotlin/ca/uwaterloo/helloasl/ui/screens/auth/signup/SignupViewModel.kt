package ca.uwaterloo.helloasl.ui.screens.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class SignupViewModel {

    private val _state = mutableStateOf(SignupState())
    val state: State<SignupState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _state.value = _state.value.copy(confirmPassword = value)
    }

    fun onCreateAccount() {
        if (_state.value.password != _state.value.confirmPassword) {
            _state.value = _state.value.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }

        // TODO: signup logic
    }
}
