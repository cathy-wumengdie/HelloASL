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
    onUpdateLessonTitle: (String) -> Unit,
    onOpenStarred: () -> Unit
) {
    when (route) {
        LearningRoute.LEARNING_HOME -> LearningView(
            vm = vm,
            onOpenStarred = {
                onOpenStarred()
            },
            onOpenLesson = { title ->
                onUpdateLessonTitle(title)
                lessonVm.state = lessonVm.state.copy(title = title)
                onNavigate(LearningRoute.LESSON)
            }
        )
        LearningRoute.LESSON -> LessonView(
            state = lessonVm.state
        )
    }
}

enum class LearningRoute {
    LEARNING_HOME,
    LESSON
}
