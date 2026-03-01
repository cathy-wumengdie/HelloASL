package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileDestination
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileView
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    vm: ProfileViewModel,
    onSettings: () -> Unit,
    onWordsLearned: () -> Unit,
    onStarredSigns: () -> Unit,
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
                ProfileDestination.ACCOUNT -> onAccount()
                ProfileDestination.LICENSE -> onLicense()
                ProfileDestination.SIGN_IN -> onSignOut()
            }
        }
    }

    ProfileView(vm = vm)
}