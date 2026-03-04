package ca.uwaterloo.helloasl.ui.screens.learning

data class LessonUIState(
    val title: String = "Lesson",
    val options: List<String> = emptyList(),
    val videoUrl: String? = null,
    val selected: String? = null,
    val isCorrect: Boolean? = null,
    val showNext: Boolean = false,
    val progress: String = ""
)
