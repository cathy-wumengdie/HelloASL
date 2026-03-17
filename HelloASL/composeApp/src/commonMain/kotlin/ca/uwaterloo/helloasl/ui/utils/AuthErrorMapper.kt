package ca.uwaterloo.helloasl.ui.utils

object AuthErrorMapper {
    fun friendlyMessage(error: Throwable): String {
        val msg = error.message.orEmpty().lowercase()

        return when {
            "email not confirmed" in msg ||
                    "email_not_confirmed" in msg ->
                "Please verify your email before signing in."

            "invalid login credentials" in msg ||
                    "invalid credentials" in msg ||
                    "invalid_credentials" in msg ->
                "Invalid email or password"

            "user already registered" in msg ||
                    "already registered" in msg ||
                    "email_exists" in msg ||
                    "user_already_exists" in msg ->
                "Email already exists"

            "weak password" in msg ||
                    "weak_password" in msg ->
                "Password is too weak"

            "email rate limit exceeded" in msg ||
                    "over_request_rate_limit" in msg ||
                    "over_email_send_rate_limit" in msg ->
                "Email rate limit exceeded. Please wait and try again."

            "invalid email" in msg ||
                    "email address is invalid" in msg ||
                    "email_address_invalid" in msg ||
                    "validation_failed" in msg ->
                "Please enter a valid email"

            else ->
                "Something went wrong. Please try again."
        }
    }
}