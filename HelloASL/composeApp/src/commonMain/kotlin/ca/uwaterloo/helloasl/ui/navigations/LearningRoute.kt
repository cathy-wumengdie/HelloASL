package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import ca.uwaterloo.helloasl.ui.screens.learning.LearningEntry
import ca.uwaterloo.helloasl.ui.screens.learning.LearningRoute
import ca.uwaterloo.helloasl.ui.screens.learning.LearningViewModel
import ca.uwaterloo.helloasl.ui.screens.learning.LessonViewModel

@Composable
fun LearningRoute(
    vm: LearningViewModel,
    lessonVm: LessonViewModel,
    route: LearningRoute,
    onNavigate: (LearningRoute) -> Unit,
    onUpdateLessonTitle: (String) -> Unit,
    onOpenStarred: () -> Unit
) {
    LearningEntry(
        vm = vm,
        lessonVm = lessonVm,
        route = route,
        onNavigate = onNavigate,
        onUpdateLessonTitle = onUpdateLessonTitle,
        onOpenStarred = onOpenStarred
    )
}