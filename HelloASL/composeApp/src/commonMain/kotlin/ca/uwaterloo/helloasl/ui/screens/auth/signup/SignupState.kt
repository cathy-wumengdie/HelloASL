package ca.uwaterloo.helloasl.ui.screens.auth.signup

data class SignupState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null
)
