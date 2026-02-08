package ca.uwaterloo.helloasl.ui.screens.learning

data class LearningState(
    val starredCount: Int = 12,
    val signsCount: Int = 12,
    val alphabetScore: Int = 90,
    val alphabetAGDone: Boolean = true,
    val alphabetHPUnlocked: Boolean = true,
    val alphabetQZLocked: Boolean = true,

    // Greetings
    val greetingsHelloLocked: Boolean = true,
)
