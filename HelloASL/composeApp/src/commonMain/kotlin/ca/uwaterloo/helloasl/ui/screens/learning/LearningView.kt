package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.domain.learningModel.Lesson

@Composable
fun LearningView(
    onOpenLesson: (lessonId: Int) -> Unit,
    onOpenStarred: () -> Unit,
    vm: LearningViewModel
) {
    val state = vm.state
    val moduleTitle = state.modules.firstOrNull()?.title ?: "Learning"
    val lessons = state.modules.firstOrNull()?.lessonIds?.mapNotNull { id -> state.lessons.find { it.id == id } }
        ?: state.lessons

    LaunchedEffect(vm) {
        vm.navEvents.collectLatest { event ->
            when (event.dest) {
                LearningDestination.LESSON -> onOpenLesson(event.lessonId as Int)
                LearningDestination.STARRED -> onOpenStarred()
            }
        }
    }

    val pageBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)
    val cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    val innerBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.40f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Starred / Signs
        HelloASLCard(cardColor = cardBg, elevationDp = 0.dp) {
            ClickableSection(
                onClick = vm::onOpenStarred,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Starred", style = MaterialTheme.typography.titleSmall)
                        Text("Review saved signs", style = MaterialTheme.typography.bodySmall)
                    }
                    Text((state.starredCount).toString())
                }
            }
        }

        // Module + lessons
        HelloASLCard(cardColor = cardBg, elevationDp = 0.dp) {
            Text(moduleTitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                lessons.forEach { lesson ->
                    LessonRow(lesson = lesson, enabled = !lesson.locked) {
                        if (!lesson.locked) vm.onOpenLesson(lesson.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    ClickableSection(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
        shape = shape,
        padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(lesson.title, style = MaterialTheme.typography.titleSmall)
                Text("${lesson.signIds.size} signs", style = MaterialTheme.typography.bodySmall)
            }
            if (!enabled) {
                Icon(Icons.Filled.Lock, contentDescription = null)
            }
        }
    }
}
