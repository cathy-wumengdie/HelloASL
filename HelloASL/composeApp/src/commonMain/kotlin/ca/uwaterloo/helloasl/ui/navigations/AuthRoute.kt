package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.ui.screens.auth.login.LoginView
import ca.uwaterloo.helloasl.ui.screens.auth.login.LoginViewModel
import ca.uwaterloo.helloasl.ui.screens.auth.signup.SignupView
import ca.uwaterloo.helloasl.ui.screens.auth.signup.SignupViewModel

@Composable
fun AuthRouteHost(
    route: AuthRoute,
    loginVm: LoginViewModel,
    signupVm: SignupViewModel,
    onRouteChange: (AuthRoute) -> Unit,
    onLoginSuccess: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    when (route) {
        AuthRoute.LOGIN -> LoginView(
            viewModel = loginVm,
            onNavigateToSignup = {
                signupVm.resetState()
                onRouteChange(AuthRoute.SIGNUP)
            },
            onLoginSuccess = onLoginSuccess
        )

        AuthRoute.SIGNUP -> SignupView(
            viewModel = signupVm,
            onBackToLogin = {
                onRouteChange(AuthRoute.LOGIN)
            },
            onSignupSuccess = onSignupSuccess
        )
    }
}