package ca.uwaterloo.helloasl.ui.screens.auth.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import kotlinx.coroutines.launch

@Composable
fun SignupView(
    viewModel: SignupViewModel,
    onBackToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val state = viewModel.uiState.value
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(48.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        HelloASLCard {
            if (state.verificationEmailSent) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Account created successfully.",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = state.infoMessage ?: "Please verify your email before logging in.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = onBackToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Sign In")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.onCreateAccount(onSuccess = onSignupSuccess, onNeedsVerification = { })
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("Create Account")
                        }
                    }

                    state.infoMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    state.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        ClickableSection(onClick = onBackToLogin) {
            Text(
                text = "Back to Sign In",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
