package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.NumberWheelPicker

@Composable
fun ProfileView(vm: ProfileViewModel) {
    val state = vm.state
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
                    ClickableSection(
                        onClick = vm::onWordsLearned,
                        modifier = Modifier.weight(1f)
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
                    Text("Learn ${state.learningGoalPerDay} minutes per day", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Learn ${state.learningGoalPerWeek} days per week", style = MaterialTheme.typography.titleMedium)
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
                TextButton(onClick = vm::onLicense) {
                    Text("License", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = vm::onSignOut) {
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
