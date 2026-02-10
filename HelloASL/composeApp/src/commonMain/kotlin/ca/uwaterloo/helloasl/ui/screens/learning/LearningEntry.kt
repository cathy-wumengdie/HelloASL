package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun LearningEntry(
    vm: LearningViewModel,
    lessonVm: LessonViewModel,
    route: LearningRoute,
    onNavigate: (LearningRoute) -> Unit,
    onUpdateLessonTitle: (String) -> Unit
) {
    when (route) {
        LearningRoute.LEARNING_HOME -> LearningView(
            vm = vm,
            onOpenStarred = {
                onNavigate(LearningRoute.STARRED)
            },
            onOpenLesson = { title ->
                onUpdateLessonTitle(title)
                lessonVm.state = lessonVm.state.copy(title = title)
                onNavigate(LearningRoute.LESSON)
            }
        )
        LearningRoute.LESSON -> LessonView(
            state = lessonVm.state,
            onBack = { onNavigate(LearningRoute.LEARNING_HOME) }
        )
        LearningRoute.STARRED -> {
             // temp
             Box { Text("Starred") }
        }
    }
}

enum class LearningRoute {
    LEARNING_HOME,
    LESSON,
    STARRED
}
