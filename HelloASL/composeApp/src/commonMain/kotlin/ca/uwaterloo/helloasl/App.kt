package ca.uwaterloo.helloasl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.SupabaseAppDependency
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
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
    onPermissionGateCompleted: () -> Unit,
    supabaseDependency: SupabaseAppDependency? = null
) {
    HelloASLTheme {
        val repositories = remember {
            val db = MockDB()
            Repositories(
                auth = supabaseDependency?.authRepository ?: MockAuthRepository(db),
                user = supabaseDependency?.userRepository ?: MockUserRepository(db),
                star = MockStarRepository(db),
                learning = supabaseDependency?.learningRepository ?: MockLearningRepository(db),
                translate = supabaseDependency?. translateRepository ?: MockTranslateRepository(db),
                progressTracker = supabaseDependency?.progressTrackerRepository ?: MockProgressTrackerRepository(db),
            )
        }

        println("Auth repo in use: ${repositories.auth::class.simpleName}")
        println("User repo in use: ${repositories.user::class.simpleName}")
        println("Star repo in use: ${repositories.star::class.simpleName}")
        println("Learning repo in use: ${repositories.learning::class.simpleName}")
        println("Translate repo in use: ${repositories.translate::class.simpleName}")
        println("ProgressTracker repo in use: ${repositories.progressTracker::class.simpleName}")

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