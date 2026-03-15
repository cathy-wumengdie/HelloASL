package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ca.uwaterloo.helloasl.ui.navigations.HomeDestination
import ca.uwaterloo.helloasl.ui.navigations.HomeNavEvent


class HomeViewModel(private val model: Model) {
    var state by mutableStateOf(buildState())
        private set

    private fun buildState(): HomeUiState {
        val user = model.getUser()
        val progressSummary = model.getProgressSummary()
        val learningProgress = model.getUserLearningProgress()

        val module = model.getModule(learningProgress.moduleId)
        val totalLessons = module.lessonIds.size

        val lessonsCompleted = when {
            learningProgress.lessonId == -1 -> totalLessons
            else -> {
                val currentLessonIndex = module.lessonIds.indexOf(learningProgress.lessonId)
                if (currentLessonIndex == -1) totalLessons else currentLessonIndex
            }
        }

        return HomeUiState(
            userName = user.name,
            moduleTitle = module.title,
            totalLessonsInModule = totalLessons,
            lessonsCompleted = lessonsCompleted,
            streakDays = progressSummary.dayStreak,
            dailyGoalsDone = progressSummary.dailyProgress.minutesLearned,
            dailyGoalsTotal = progressSummary.dailyProgress.dailyGoalMinutes,
            weeklyGoalsDone = progressSummary.weeklyProgress.daysCompleted,
            weeklyGoalsTotal = progressSummary.weeklyProgress.weeklyGoalDays
        )
    }

    fun refresh() {
        state = buildState()
    }

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