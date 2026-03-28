package ca.uwaterloo.helloasl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

@Composable
fun PermissionsGateScreen(
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    cameraErrorMessage: String?,
    notificationGranted: Boolean,
    onRequestCamera: () -> Unit,
    onRequestNotifications: () -> Unit,
    onContinue: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Set up permissions",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You can change these later in settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            HelloASLCard(
                modifier = Modifier.fillMaxWidth(),
                cardColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Spacer(Modifier.height(4.dp))

                PermissionCardHeader(
                    icon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                    title = "Camera"
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = when {
                        cameraErrorMessage != null ->
                            "Camera unavailable on this desktop build."
                        hasCameraHardware ->
                            "Needed for ASL -> English translation."
                        else ->
                            "This device has no camera. ASL -> English will be disabled."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                val statusText = when {
                    cameraGranted -> "Allowed"
                    cameraErrorMessage != null -> "Unavailable"
                    !hasCameraHardware -> "Not available"
                    else -> "Not allowed"
                }

                val canRequest =
                    cameraErrorMessage == null &&
                            hasCameraHardware &&
                            !cameraGranted

                PermissionFooter(
                    statusText = statusText,
                    actionText = if (canRequest) "Allow" else null,
                    onAction = if (canRequest) onRequestCamera else null
                )
            }

            Spacer(Modifier.height(12.dp))

            HelloASLCard(
                modifier = Modifier.fillMaxWidth(),
                cardColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Spacer(Modifier.height(4.dp))

                PermissionCardHeader(
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                    title = "Notifications"
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Get reminders for learning goals, streaks and your learning progress.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                PermissionFooter(
                    statusText = if (notificationGranted) "Allowed" else "Not allowed",
                    actionText = if (notificationGranted) null else "Allow",
                    onAction = if (notificationGranted) null else onRequestNotifications
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Continue")
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Tip: If you tap Continue without allowing, the app still works (some features may be limited).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionCardHeader(
    icon: @Composable () -> Unit,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun PermissionFooter(
    statusText: String,
    actionText: String?,
    onAction: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Status: $statusText",
            style = MaterialTheme.typography.bodyMedium
        )

        if (actionText != null && onAction != null) {
            OutlinedButton(
                onClick = onAction,
                shape = MaterialTheme.shapes.large
            ) {
                Text(actionText)
            }
        }
    }
}