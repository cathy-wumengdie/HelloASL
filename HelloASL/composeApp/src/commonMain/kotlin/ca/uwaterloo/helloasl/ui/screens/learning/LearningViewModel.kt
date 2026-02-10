package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.lifecycle.ViewModel
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

data class LearningNavEvent(val dest: LearningDestination, val data: Any? = null)

class LearningViewModel : ViewModel() {
    var state by mutableStateOf(LearningModel())
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

    fun onOpenAlphabet(range: String) {
        // Here we could update state if needed, or just navigate
        _navEvents.tryEmit(LearningNavEvent(LearningDestination.LESSON, data = range))
    }

    fun onOpenGreetings(topic: String) {
        _navEvents.tryEmit(LearningNavEvent(LearningDestination.LESSON, data = topic))
    }
}