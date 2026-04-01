package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.toLowerCase
import ca.uwaterloo.helloasl.getPlatform
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.NumberWheelPicker
import ca.uwaterloo.helloasl.ui.components.PasswordTextField
import ca.uwaterloo.helloasl.ui.utils.cameraNoHardwareMessage
import ca.uwaterloo.helloasl.ui.utils.cameraUnavailableMessage
import com.russhwolf.settings.Settings
import jdk.internal.net.http.common.Log
import kotlinx.coroutines.launch

@Composable
fun ProfileView(vm: ProfileViewModel) {
    LaunchedEffect(Unit) { vm.refresh() }
    val state = vm.state
    val scope = rememberCoroutineScope()
    var showSetGoalsPopup by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            name = state.userName,
            avatarText = state.avatarText
        )

        Column(modifier = Modifier.padding(16.dp)) {
            HelloASLCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Learning Progress",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        NumberedCircleBadge(state.wordsLearned)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Words Learned",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    ClickableSection(
                        onClick = vm::onStarredSigns,
                        modifier = Modifier.weight(1f)
                    ) {
                        NumberedCircleBadge(state.starredSigns)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Starred Signs",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            HelloASLCard(modifier = Modifier.fillMaxWidth()) {
                ClickableSection(
                    onClick = { showSetGoalsPopup = true },
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Set Learning Goals",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (state.learningGoalPerDay > 0) "Learn ${state.learningGoalPerDay} minutes per day" else "Daily goal not set",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (state.learningGoalPerWeek > 0) "Learn ${state.learningGoalPerWeek} days per week" else "Weekly goal not set",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (showSetGoalsPopup) {
                    SetGoalsDialog(
                        initialMinutes = state.learningGoalPerDay,
                        initialDays = state.learningGoalPerWeek,
                        onDismiss = { showSetGoalsPopup = false },
                        onSave = { minutes, days ->
                            vm.onSaveLearningGoals(minutes, days)
                            showSetGoalsPopup = false
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            HelloASLCard(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = vm::onAccount) {
                    Text("Account", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = vm::onSettings) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = {
                    scope.launch {
                        vm.onSignOut()
                    }
                }) {
                    Text("Sign out", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, avatarText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val waveHeight = height * 0.35f

            val path = Path().apply {
                moveTo(0f, height - waveHeight)

                // Big smooth curve to the right
                quadraticTo(
                    width * 0.5f, height,
                    width, height - waveHeight * 0.6f
                )

                // Close shape down to bottom
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(path = path, color = Color.White.copy(alpha = 0.92f))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    )
                    .border(3.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.height(10.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

enum class GoalStep {
    MINUTES,
    DAYS
}

@Composable
fun SetGoalsDialog(
    initialMinutes: Int,
    initialDays: Int,
    onDismiss: () -> Unit,
    onSave: (minutesPerDay: Int, daysPerWeek: Int) -> Unit
) {
    var minutesPerDay by rememberSaveable { mutableIntStateOf(initialMinutes) }
    var daysPerWeek by rememberSaveable { mutableIntStateOf(initialDays) }
    var setGoalStep by rememberSaveable { mutableStateOf(GoalStep.MINUTES) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Set Learning Goals",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(20.dp))
                when (setGoalStep) {
                    GoalStep.MINUTES -> {
                        Text("Minutes per day: $minutesPerDay")
                        NumberWheelPicker(
                            value = minutesPerDay,
                            range = 5..120,
                            onValueChange = { minutesPerDay = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    GoalStep.DAYS -> {
                        Text("Days per week: $daysPerWeek")
                        NumberWheelPicker(
                            value = daysPerWeek,
                            range = 1..7,
                            onValueChange = { daysPerWeek = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        if (setGoalStep == GoalStep.DAYS)
                            setGoalStep = GoalStep.MINUTES
                        else onDismiss()
                    }) {
                        Text(if (setGoalStep == GoalStep.DAYS) "Back" else "Cancel")
                    }
                    Button(
                        onClick = {
                            if (setGoalStep == GoalStep.MINUTES) {
                                setGoalStep = GoalStep.DAYS
                            } else {
                                onSave(minutesPerDay, daysPerWeek)
                            }
                        }
                    ) {
                        Text(if (setGoalStep == GoalStep.MINUTES) "Next" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberedCircleBadge(
    number: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 30.sp
            )
        }
    }
}

@Composable
fun AccountView(
    vm: ProfileViewModel,
    email: String,
    name: String,
    onEditName: (String) -> Unit,
    onEditPassword: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(vm.passwordSuccess) {
        if (vm.passwordSuccess) {
            showEditPasswordDialog = false
        }
    }

    LaunchedEffect(vm.nameSuccess) {
        if (vm.nameSuccess) {
            showEditNameDialog = false
            vm.refresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                "Account",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(Modifier.height(20.dp))

        // Email
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Text("Email", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                email,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        // Edit Name
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Text("Name", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(name)

            Spacer(Modifier.height(6.dp))

            TextButton(onClick = { showEditNameDialog = true }) {
                Text("Edit Name", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Edit Password
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Text("Password", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(6.dp))

            TextButton(onClick = { showEditPasswordDialog = true }) {
                Text("Edit Password", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Name Dialog
    if (showEditNameDialog) {
        NameDialog(
            title = "Edit Name",
            initialValue = name,
            onDismiss = { showEditNameDialog = false },
            onSave = {
                onEditName(it)
            },
            errorMessage = vm.nameError
        )
    }

    // Password Dialog
    if (showEditPasswordDialog) {
        PasswordDialog(
            onDismiss = { showEditPasswordDialog = false },
            onSave = { current, new ->
                onEditPassword(current, new)
            } ,
            errorMessage = vm.passwordError
        )
    }
}

@Composable
fun NameDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    errorMessage:String?
) {
    var text by remember { mutableStateOf(initialValue) }
    var localError by remember { mutableStateOf<String?>(null) }
    val displayError = localError ?: errorMessage

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    localError = null
                },
                singleLine = true
            )
            if (displayError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = displayError,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    text.isBlank() -> localError = "Name cannot be empty"
                    else -> {
                        localError = null
                        onSave(text)
                    }
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onSave: (currentPassword: String, newPassword: String) -> Unit,
    errorMessage: String?
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val displayError = localError ?: errorMessage

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                PasswordTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = "Current Password",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                PasswordTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = "New Password",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                PasswordTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = "Confirm New Password",
                    modifier = Modifier.fillMaxWidth()
                )

                if (displayError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = displayError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    current.isBlank() -> localError = "Please enter your current password"
                    new.isBlank() -> localError = "New password cannot be empty"
                    new != confirm -> localError = "Passwords do not match"
                    else -> {
                        localError = null
                        onSave(current, new)
                    }
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SettingsScreen(
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    cameraErrorMessage: String?,
    notificationGranted: Boolean,
    onRequestCamera: () -> Unit,
    onRequestNotifications: () -> Unit,
    onDone: () -> Unit
) {
    val platform = getPlatform()
    val osName = System.getProperty("os.name")
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
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(28.dp))

            HelloASLCard(
                modifier = Modifier.fillMaxWidth(),
                cardColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Spacer(Modifier.height(4.dp))

                SettingsCardHeader(
                    icon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                    title = "Camera"
                )

                Spacer(Modifier.height(8.dp))

                val noCameraText = cameraNoHardwareMessage(platform, osName)

                Text(
                    text = when {
                        platform.isDesktop ->
                            noCameraText
                        hasCameraHardware ->
                            "Needed for ASL -> English translation."
                        else ->
                            noCameraText
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                val statusText = when {
                    cameraGranted -> "Allowed"
                    cameraErrorMessage != null &&
                            !cameraErrorMessage.lowercase().contains("denied") -> "Unavailable"
                    !hasCameraHardware -> "Not available"
                    else -> "Not allowed"
                }

                val showAction =
                    (cameraErrorMessage == null ||
                            cameraErrorMessage.lowercase().contains("denied")) &&
                            hasCameraHardware

                SettingsFooter(
                    statusText = statusText,
                    actionText = if (showAction) "Manage" else null,
                    onAction = if (showAction) onRequestCamera else null
                )
            }

            if (platform.isAndroid) {
                Spacer(Modifier.height(12.dp))

                HelloASLCard(
                    modifier = Modifier.fillMaxWidth(),
                    cardColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Spacer(Modifier.height(4.dp))

                    SettingsCardHeader(
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        title = "Notifications"
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Get reminders for learning goals, streaks and your learning progress.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    SettingsFooter(
                        statusText = if (notificationGranted) "Allowed" else "Not allowed",
                        actionText = "Manage",
                        onAction = onRequestNotifications
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun SettingsCardHeader(
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
private fun SettingsFooter(
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
