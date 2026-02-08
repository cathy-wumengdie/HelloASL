package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

@Composable
fun LessonScreen(
    state: LessonState,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(state.title, style = MaterialTheme.typography.titleLarge)

        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
            }
        }

        Text("What does this sign mean?", style = MaterialTheme.typography.titleMedium)

        state.options.forEach {
            OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text(it) }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
