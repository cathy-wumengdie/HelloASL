package ca.uwaterloo.helloasl.ui.screens.learning

enum class LessonPhase {
    VIEWING,
    QUIZ
}

data class LessonUIState(
    val title: String = "Lesson",
    val phase: LessonPhase = LessonPhase.VIEWING,
    val signIndex: Int = 0,
    val signTotal: Int = 0,
    val signGloss: String = "",
    val videoUrl: String? = null,
    val canPrevSign: Boolean = false,
    val canNextSign: Boolean = false,
    val canPrevVideo: Boolean = false,
    val canNextVideo: Boolean = false,
    val options: List<String> = emptyList(),
    val selected: String? = null,
    val isCorrect: Boolean? = null,
    val showNext: Boolean = false,
    val showStartQuiz: Boolean = false,
    val progress: String = ""
)