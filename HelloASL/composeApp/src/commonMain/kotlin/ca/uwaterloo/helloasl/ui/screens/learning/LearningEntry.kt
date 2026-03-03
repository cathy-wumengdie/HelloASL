package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.runtime.Composable

@Composable
fun LearningEntry(
    vm: LearningViewModel,
    lessonVm: LessonViewModel,
    route: LearningRoute,
    onNavigate: (LearningRoute) -> Unit,
    onUpdateLessonTitle: (String) -> Unit,
    onOpenStarred: () -> Unit
) {
    lessonVm.setOnLessonCompleted { completedId ->
        vm.unlockNext(completedId)
    }

    when (route) {
        LearningRoute.LEARNING_HOME -> LearningView(
            vm = vm,
            onOpenStarred = { onOpenStarred() },
            onOpenLesson = { lessonId ->
                lessonVm.loadLesson(lessonId)
                onUpdateLessonTitle(lessonVm.state.title)
                onNavigate(LearningRoute.LESSON)
            }
        )
        LearningRoute.LESSON -> LessonView(vm = lessonVm)
    }
}

enum class LearningRoute {
    LEARNING_HOME,
    LESSON
}
