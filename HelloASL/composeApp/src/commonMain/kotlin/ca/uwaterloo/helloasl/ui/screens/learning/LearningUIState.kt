package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.Lesson

data class LessonItem(
    val lessonId: Long,
    val title: String,
    val signCount: Int,
    val locked: Boolean
)

data class LearningUIState(
    val starredCount: Int = 0,
    val modules: List<Module> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
    val lessonItems: List<LessonItem> = emptyList()
)