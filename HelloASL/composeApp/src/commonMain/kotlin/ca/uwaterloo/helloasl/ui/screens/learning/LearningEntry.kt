package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.runtime.*

@Composable
fun LearningEntry() {
    val vm = remember { LearningViewModel() }
    val lessonVm = remember { LessonViewModel() }

    var route by remember { mutableStateOf("learning") }
    var lessonTitle by remember { mutableStateOf("Alphabet: H–P") }

    when (route) {
        "learning" -> LearningScreen(
            state = vm.state,
            onOpenStarred = { vm.onOpenStarred() /* TODO */ },
            onOpenLesson = { title ->
                vm.onOpenAlphabet(title)
                lessonTitle = title
                lessonVm.state = lessonVm.state.copy(title = title) // 如果你把 state 设为 private set，这行去掉即可
                route = "lesson"
            }
        )
        "lesson" -> LessonScreen(
            state = lessonVm.state.copy(title = lessonTitle),
            onBack = { route = "learning" }
        )
    }
}
