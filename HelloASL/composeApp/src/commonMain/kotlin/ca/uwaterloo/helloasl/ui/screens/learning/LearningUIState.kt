package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.domain.learning.Module
import ca.uwaterloo.helloasl.domain.learning.Lesson

data class LearningUIState(
    val starredCount: Int = 12,
    val signsCount: Int = 12,
    val alphabetScore: Int = 90,
    val alphabetAGDone: Boolean = true,
    val alphabetHPUnlocked: Boolean = true,
    val alphabetQZLocked: Boolean = true,

    // Greetings
    val greetingsHelloLocked: Boolean = true,

    // Modules
    val modules: List<Module> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
)
