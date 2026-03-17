package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.domain.Model
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class LearningDestination {
    LESSON,
    STARRED
}

data class LearningNavEvent(val dest: LearningDestination, val lessonId: Long? = null)

class LearningViewModel(private val model: Model) {
    var state by mutableStateOf(LearningUIState())
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        refresh()
    }

    private suspend fun buildState(): LearningUIState {
        model.prepareLessonLocks()
        val modules = model.getModules()
        val lessons = model.getLessons()
        val lessonItems: List<LessonItem> = lessons
            .sortedWith(compareBy<Lesson> { it.moduleId }.thenBy { it.lessonId })
            .map { lesson ->
                LessonItem(
                    lessonId = lesson.lessonId,
                    title = lesson.title,
                    signCount = model.getSignCountForLesson(lesson.lessonId),
                    locked = model.isLessonLocked(lesson.lessonId)
                )
            }

        return LearningUIState(
            starredCount = model.getStarredSigns().size,
            modules = modules,
            lessons = lessons,
            lessonItems = lessonItems
        )
    }

    fun refresh() {
        scope.launch {
            state = buildState()
        }
    }

    private val _navEvents = MutableSharedFlow<LearningNavEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents: SharedFlow<LearningNavEvent> = _navEvents.asSharedFlow()

    fun onOpenStarred() {
        _navEvents.tryEmit(LearningNavEvent(LearningDestination.STARRED))
    }

    fun onOpenLesson(lessonId: Long) {
        _navEvents.tryEmit(LearningNavEvent(LearningDestination.LESSON, lessonId = lessonId))
    }

    fun onOpenAlphabet(lessonId: Long) {
        onOpenLesson(lessonId)
    }

    fun onOpenGreetings(lessonId: Long) {
        onOpenLesson(lessonId)
    }

    fun unlockNext(completedLessonId: Long) {
        val lessonsList = state.lessonItems
        val currentIndex = lessonsList.indexOfFirst { it.lessonId == completedLessonId }
        if (currentIndex == -1) return
        val nextIndex = currentIndex + 1
        if (nextIndex >= lessonsList.size) return
        val nextId = lessonsList[nextIndex].lessonId
        model.unlockLesson(nextId)
        refresh()
    }
}