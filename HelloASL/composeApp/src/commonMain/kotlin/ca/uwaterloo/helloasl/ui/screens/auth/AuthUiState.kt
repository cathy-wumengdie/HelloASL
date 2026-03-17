package ca.uwaterloo.helloasl.ui.screens.auth

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object LoggedOut : AuthUiState
    data object LoggedIn : AuthUiState
}