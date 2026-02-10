package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import ca.uwaterloo.helloasl.LearningRoute

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
                // 修复：这里直接请求导航，不要再调用 vm.onOpenStarred() 否则会死循环
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
             // 临时占位
             Box { Text("Starred") }
        }
    }
}
