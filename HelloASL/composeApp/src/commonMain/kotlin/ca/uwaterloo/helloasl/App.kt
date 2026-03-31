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
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository

@Composable
fun App(
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    cameraErrorMessage: String?,
    notificationGranted: Boolean,
    requestCameraPermission: () -> Unit,
    requestNotificationPermission: () -> Unit,
    hasSeenPermissionGate: Boolean,
    onPermissionGateCompleted: () -> Unit,
    supabaseDependency: SupabaseAppDependency? = null,
    onLoginSuccessSyncDeviceToken: suspend () -> Boolean,
) {
    HelloASLTheme {
        val repositories = remember {
            val db = MockDB()
            Repositories(
                auth = supabaseDependency?.authRepository ?: MockAuthRepository(db),
                user = supabaseDependency?.userRepository ?: MockUserRepository(db),
                star = supabaseDependency?.starRepository ?: MockStarRepository(db),
                learning = supabaseDependency?.learningRepository ?: MockLearningRepository(db),
                translate = supabaseDependency?. translateRepository ?: MockTranslateRepository(db),
                progressTracker = supabaseDependency?.progressTrackerRepository ?: MockProgressTrackerRepository(db),
                notification = supabaseDependency?.notificationRepository ?: NoOpNotificationRepository
            )
        }

        println("Auth repo in use: ${repositories.auth::class.simpleName}")
        println("User repo in use: ${repositories.user::class.simpleName}")
        println("Star repo in use: ${repositories.star::class.simpleName}")
        println("Learning repo in use: ${repositories.learning::class.simpleName}")
        println("Translate repo in use: ${repositories.translate::class.simpleName}")
        println("ProgressTracker repo in use: ${repositories.progressTracker::class.simpleName}")
        println("Notification repo in use: ${repositories.notification::class.simpleName}")

        val model = remember { Model(repositories) }

        androidx.compose.foundation.layout.Box {

            AppNavigation(
                model = model,
                hasCameraHardware = hasCameraHardware,
                cameraGranted = cameraGranted,
                cameraErrorMessage = cameraErrorMessage,
                notificationGranted = notificationGranted,
                requestCameraPermission = requestCameraPermission,
                requestNotificationPermission = requestNotificationPermission,
                hasSeenPermissionGate = hasSeenPermissionGate,
                onPermissionGateCompleted = onPermissionGateCompleted,
                onLoginSuccessSyncDeviceToken = onLoginSuccessSyncDeviceToken,
            )

            if (model.tagDialogVisible) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { model.dismissTagDialog() },

                    containerColor = Color(0xFFFFF8E1),
                    titleContentColor = Color(0xFF5D4037),
                    textContentColor = Color(0xFF6D4C41),

                    title = {
                        androidx.compose.material3.Text("Add Tag")
                    },

                    text = {
                        androidx.compose.foundation.layout.Column {
                            model.availableStarTags.forEach { tag ->
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        kotlinx.coroutines.CoroutineScope(
                                            kotlinx.coroutines.Dispatchers.Main
                                        ).launch {
                                            model.confirmStarWithTag(tag.id)
                                        }
                                    }
                                ) {
                                    androidx.compose.material3.Text(tag.name)
                                }
                            }

                            androidx.compose.material3.TextButton(
                                onClick = {
                                    model.showCreateTagInput = true
                                }
                            ) {
                                androidx.compose.material3.Text("+ New Collection")
                            }

                            if (model.showCreateTagInput) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = model.newTagName,
                                    onValueChange = { model.newTagName = it },
                                    label = { androidx.compose.material3.Text("Collection name") }
                                )

                                androidx.compose.material3.Button(
                                    onClick = {
                                        kotlinx.coroutines.CoroutineScope(
                                            kotlinx.coroutines.Dispatchers.Main
                                        ).launch {
                                            println("CLICK CREATE: ${model.newTagName}")

                                            model.createTag(model.newTagName)
                                            model.showCreateTagInput = false
                                            model.newTagName = ""
                                        }
                                    }
                                ) {
                                    androidx.compose.material3.Text("Create")
                                }
                            }
                        }
                    },



                    confirmButton = {}
                )
            }
        }
    }
}