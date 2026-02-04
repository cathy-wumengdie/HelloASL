package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

@Composable
fun HomeScreen(
    state: HomeState,
    onContinueLearning: () -> Unit,
    onDayStreak: () -> Unit,
    onDailyGoals: () -> Unit,
    onLearnAsl: () -> Unit,
    onTakeQuiz: () -> Unit,
    onTranslate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Your Progress", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Text(state.moduleTitle, style = MaterialTheme.typography.titleMedium)
                Text(state.lessonProgress, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onContinueLearning,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Continue Learning") }
            }
        }
        Spacer(Modifier.height(16.dp))
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDayStreak) {
                        Text(
                            "${state.streakDays} Day Streak",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDailyGoals) {
                        Text(
                            "${state.goalsDone} / ${state.goalsTotal} Daily Goals",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HelloASLCard(modifier = Modifier.weight(1f)) {
                Column {
                    IconButton(onClick = onLearnAsl) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = "Learning",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    TextButton(onClick = onLearnAsl) {
                        Text(
                            "Learn ASL",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = onTakeQuiz) {
                        Text(
                            "Take quiz",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            HelloASLCard(modifier = Modifier.weight(1f)) {
                Column {
                    IconButton(onClick = onTranslate) {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = "Translate",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    TextButton(onClick = onTranslate) {
                        Text(
                            "Translate ASL",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = onTranslate) {
                        Text(
                            "English <-> ASL",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}