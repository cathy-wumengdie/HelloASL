package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import kotlinx.coroutines.delay

@Composable
fun LessonView(
    vm: LessonViewModel
) {
    // Start timer when entering lesson screen
    LaunchedEffect(Unit) { vm.onEnterLesson() }
    // Stop timer + commit minutes when leaving lesson screen
    DisposableEffect(Unit) { onDispose { vm.onExitLesson() } }

    val state = vm.state

    LaunchedEffect(state.phase) {
        if (state.phase == LessonPhase.VIEWING) {
            while (true) {
                vm.refreshCurrentStarState()
                delay(300)
            }
        }
    }

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
            Box(modifier = Modifier.fillMaxWidth()) {
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

                if (state.canPrevVideo) {
                    IconButton(
                        onClick = { vm.onPrevVideo() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous video")
                    }
                }
                if (state.canNextVideo) {
                    IconButton(
                        onClick = { vm.onNextVideo() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next video")
                    }
                }
            }
        }

        when (state.phase) {
            LessonPhase.VIEWING -> {
                if (state.signGloss.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.signGloss,
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(onClick = { vm.onStar() }) {
                            Icon(
                                imageVector = if (state.isStarred)
                                    Icons.Filled.Star
                                else
                                    Icons.Filled.StarBorder,
                                contentDescription = "Star"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { vm.onPrevSign() },
                        enabled = state.canPrevSign,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Previous")
                    }
                    OutlinedButton(
                        onClick = { vm.onNextSign() },
                        enabled = state.canNextSign,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Next")
                    }
                }

                if (state.showStartQuiz) {
                    Button(
                        onClick = { vm.onStartQuiz() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(solidBg)
                    ) {
                        Text("Start Quiz")
                    }
                }
            }

            LessonPhase.QUIZ -> {
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
                        colors = ButtonDefaults.buttonColors(solidBg)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}
