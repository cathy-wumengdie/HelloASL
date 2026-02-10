package ca.uwaterloo.helloasl.ui.screens.auth.signup

data class SignupCredentials(
    val email: String,
    val password: String,
    val confirmPassword: String
)
