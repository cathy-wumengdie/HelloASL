package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class HomeDestination {
    LEARNING,
    DAY_STREAK,
    DAILY_GOALS,
    QUIZ,
    TRANSLATE,
    NOTIFICATIONS
}

data class HomeNavEvent(val dest: HomeDestination)

class HomeViewModel : ViewModel() {
    var state by mutableStateOf(HomeModel())
        private set

    private val _navEvents = MutableSharedFlow<HomeNavEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents: SharedFlow<HomeNavEvent> = _navEvents.asSharedFlow()

    fun onDayStreak() {
        _navEvents.tryEmit(HomeNavEvent(HomeDestination.DAY_STREAK))
    }

    fun onDailyGoals() {
        _navEvents.tryEmit(HomeNavEvent(HomeDestination.DAILY_GOALS))
    }

    fun onLearning() {
        _navEvents.tryEmit(HomeNavEvent(HomeDestination.LEARNING))
    }

    fun onTakeQuiz() {
        _navEvents.tryEmit(HomeNavEvent(HomeDestination.QUIZ))
    }

    fun onTranslate() {
        _navEvents.tryEmit(HomeNavEvent(HomeDestination.TRANSLATE))
    }

    fun onNotifications() {
        _navEvents.tryEmit(HomeNavEvent(HomeDestination.NOTIFICATIONS))
    }
}