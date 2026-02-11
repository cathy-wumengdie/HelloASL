package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

@Composable
fun LessonView(
    state: LessonModel
) {
    val pageBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)
    val cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.40f)
    val borderCol = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(state.title, style = MaterialTheme.typography.titleMedium)

        HelloASLCard(
            modifier = Modifier.fillMaxWidth(),
            cardColor = cardBg,
            elevationDp = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
            }
        }

        Text("What does this sign mean?", style = MaterialTheme.typography.titleMedium)

        state.options.forEach { opt ->
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) { Text(opt) }
        }
    }
}
