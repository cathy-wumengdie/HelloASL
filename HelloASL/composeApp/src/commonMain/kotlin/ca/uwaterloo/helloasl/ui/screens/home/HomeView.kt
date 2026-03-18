package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember

@Composable
fun HomeView(vm: HomeViewModel) {
    val state = vm.state

    LaunchedEffect(Unit) {
        vm.refresh()
    }

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
                    onClick = vm::onLearning,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Continue Learning") }
            }
        }
        Spacer(Modifier.height(16.dp))

        HelloASLCard(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.weight(0.9f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${state.streakDays} Day Streak",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                }
                Row(
                    modifier = Modifier.weight(1.3f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("🎯", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    Spacer(Modifier.width(10.dp))
                    if (state.dailyGoalsTotal == 0 || state.weeklyGoalsTotal == 0) {
                        Text(
                            "Set your learning goals on Profile page",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                "${state.dailyGoalsDone} / ${state.dailyGoalsTotal} min today",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Text(
                                "${state.weeklyGoalsDone} / ${state.weeklyGoalsTotal} days/week",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                        }
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
                    IconButton(onClick = vm::onLearning) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = "Learning",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    TextButton(onClick = vm::onLearning) {
                        Text(
                            "Learn ASL",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            HelloASLCard(modifier = Modifier.weight(1f)) {
                Column {
                    IconButton(onClick = vm::onTranslate) {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = "Translate",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    TextButton(onClick = vm::onTranslate) {
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