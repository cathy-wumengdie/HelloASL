package ca.uwaterloo.helloasl.ui.screens.learning

data class LessonState(
    val title: String = "Alphabet: H–P",
    val options: List<String> = listOf("Cat", "Dog", "Fish"),
    val locked: Boolean = false
)
