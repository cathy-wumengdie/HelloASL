package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.domain.Model
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class LearningDestination {
    LESSON,
    STARRED
}

data class LearningNavEvent(val dest: LearningDestination, val lessonId: Int? = null)

class LearningViewModel(private val model: Model) {
    var state by mutableStateOf(
        LearningUIState(
            modules = model.getModules(),
            lessons = model.getLessons()
        )
    )
        private set

    private val _navEvents = MutableSharedFlow<LearningNavEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents: SharedFlow<LearningNavEvent> = _navEvents.asSharedFlow()

    fun onOpenStarred() {
        _navEvents.tryEmit(LearningNavEvent(LearningDestination.STARRED))
    }
    fun onOpenLesson(lessonId: Int) {
        _navEvents.tryEmit(LearningNavEvent(LearningDestination.LESSON, lessonId = lessonId))
    }
    fun onOpenAlphabet(lessonId: Int) {
        onOpenLesson(lessonId)
    }

    fun onOpenGreetings(lessonId: Int) {
        onOpenLesson(lessonId)
    }

    fun unlockNext(completedLessonId: Int) {
        val lessonsList = state.lessons
        val currentIndex = lessonsList.indexOfFirst { it.id == completedLessonId }
        if (currentIndex == -1) return
        val nextIndex = currentIndex + 1
        if (nextIndex >= lessonsList.size) return
        val nextId = lessonsList[nextIndex].id
        model.unlockLesson(nextId)
        state = state.copy(lessons = model.getLessons())
    }
}