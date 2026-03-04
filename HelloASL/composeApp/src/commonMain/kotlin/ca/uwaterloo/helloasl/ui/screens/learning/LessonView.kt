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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.SignVideoPlayer

@Composable
fun LessonView(
    vm: LessonViewModel
) {
    // Start timer when entering lesson screen
    LaunchedEffect(Unit) { vm.onEnterLesson() }
    // Stop timer + commit minutes when leaving lesson screen
    DisposableEffect(Unit) { onDispose { vm.onExitLesson() } }

    val state = vm.state
    val pageBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)
    val cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.40f)
    val solidBg = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(state.title, style = MaterialTheme.typography.titleMedium)
        if (state.progress.isNotBlank()) {
            Text(state.progress, style = MaterialTheme.typography.bodySmall)
        }

        HelloASLCard(
            modifier = Modifier.fillMaxWidth(),
            cardColor = cardBg,
            elevationDp = 0.dp
        ) {
            val videoUrl = state.videoUrl
            if (videoUrl != null) {
                SignVideoPlayer(
                    resourcePath = videoUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                }
            }
        }

        Text("What does this sign mean?", style = MaterialTheme.typography.titleMedium)

        state.options.forEach { opt ->
            val isChosen = state.selected == opt
            val correctChoice = state.isCorrect == true && isChosen
            val wrongChoice = state.isCorrect == false && isChosen
            OutlinedButton(
                onClick = { vm.onChoose(opt) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                val color = when {
                    correctChoice -> solidBg
                    wrongChoice -> solidBg
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Text(opt, color = color)
            }
        }

        when (state.isCorrect) {
            true -> Text("Correct!", color = solidBg)
            false -> Text("Incorrect, try again", color = solidBg)
            null -> {}
        }

        if (state.showNext) {
            Button(
                onClick = { vm.onNext() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    solidBg
                )
            ) {
                Text("Next")
            }
        }
    }
}
