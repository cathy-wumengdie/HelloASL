package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    vm: ProfileViewModel,
    onSettings: () -> Unit,
    onWordsLearned: () -> Unit,
    onStarredSigns: () -> Unit,
    onSetLearningGoals: () -> Unit,
    onAccount: () -> Unit,
    onLicense: () -> Unit,
    onSignOut: () -> Unit
) {
    LaunchedEffect(vm) {
        vm.navEvents.collectLatest { event ->
            when (event.dest) {
                ProfileDestination.SETTINGS -> onSettings()
                ProfileDestination.WORDS_LEARNED -> onWordsLearned()
                ProfileDestination.STARRED_SIGNS -> onStarredSigns()
                ProfileDestination.SET_LEARNING_GOALS -> onSetLearningGoals()
                ProfileDestination.ACCOUNT -> onAccount()
                ProfileDestination.LICENSE -> onLicense()
                ProfileDestination.SIGN_IN -> onSignOut()
            }
        }
    }

    ProfileView(vm = vm)
}