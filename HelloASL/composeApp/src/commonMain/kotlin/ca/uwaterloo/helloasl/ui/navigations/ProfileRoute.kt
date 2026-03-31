package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.getPlatform
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

import ca.uwaterloo.helloasl.ui.screens.profile.ProfileView
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileViewModel
import ca.uwaterloo.helloasl.ui.screens.profile.AccountView
import ca.uwaterloo.helloasl.ui.screens.profile.SettingsScreen
import kotlinx.coroutines.flow.collectLatest

enum class ProfileDestination {
    SETTINGS,
    WORDS_LEARNED,
    STARRED_SIGNS,
    ACCOUNT,
    SIGN_IN
}

data class ProfileNavEvent(val dest: ProfileDestination)

@Composable
fun ProfileRoute(
    vm: ProfileViewModel,
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    cameraErrorMessage: String?,
    notificationGranted: Boolean,
    requestCameraPermission: () -> Unit,
    requestNotificationPermission: () -> Unit,
    onWordsLearned: () -> Unit,
    onStarredSigns: () -> Unit,
    onSignOut: () -> Unit
) {
    var showAccount by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.navEvents.collectLatest { event ->
            when (event.dest) {
                ProfileDestination.SETTINGS -> {
                    showSettings = true
                }
                ProfileDestination.WORDS_LEARNED -> onWordsLearned()
                ProfileDestination.STARRED_SIGNS -> onStarredSigns()
                ProfileDestination.ACCOUNT -> {
                    showAccount = true
                }
                ProfileDestination.SIGN_IN -> onSignOut()
            }
        }
    }

    when {
        showSettings -> {
            SettingsScreen(
                hasCameraHardware = hasCameraHardware,
                cameraGranted = cameraGranted,
                cameraErrorMessage = cameraErrorMessage,
                notificationGranted = notificationGranted,
                onRequestCamera = requestCameraPermission,
                onRequestNotifications = requestNotificationPermission,
                onDone = { showSettings = false }
            )
        }

        showAccount -> {
            AccountView(
                vm = vm,
                email = vm.state.email,
                name = vm.state.userName,
                onEditName = vm::onEditName,
                onEditPassword = vm::onEditPassword,
                onBack = { showAccount = false }
            )
        }

        else -> {
            ProfileView(vm = vm)
        }
    }
}
