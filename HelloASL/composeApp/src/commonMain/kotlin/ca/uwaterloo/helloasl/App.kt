package ca.uwaterloo.helloasl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.ui.navigations.AppNavigation
import ca.uwaterloo.helloasl.ui.theme.HelloASLTheme

@Composable
fun App(
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    notificationGranted: Boolean,
    requestCameraPermission: () -> Unit,
    requestNotificationPermission: () -> Unit,
    hasSeenPermissionGate: Boolean,
    onPermissionGateCompleted: () -> Unit
) {
    HelloASLTheme {
        val repositories = remember {
            val db = MockDB()
            Repositories(
                auth = MockAuthRepository(db),
                user = MockUserRepository(db),
                learning = MockLearningRepository(db),
                translate = MockTranslateRepository(db),
                progressTracker = MockProgressTrackerRepository(db),
            )
        }

        val model = remember { Model(repositories) }

        AppNavigation(
            model = model,
            hasCameraHardware = hasCameraHardware,
            cameraGranted = cameraGranted,
            notificationGranted = notificationGranted,
            requestCameraPermission = requestCameraPermission,
            requestNotificationPermission = requestNotificationPermission,
            hasSeenPermissionGate = hasSeenPermissionGate,
            onPermissionGateCompleted = onPermissionGateCompleted
        )
    }
}