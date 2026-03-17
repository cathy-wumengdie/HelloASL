package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ca.uwaterloo.helloasl.ui.navigations.HomeDestination
import ca.uwaterloo.helloasl.ui.navigations.HomeNavEvent


class HomeViewModel(private val model: Model) {
    var state by mutableStateOf(
        HomeUiState(
            userName = "",
            moduleTitle = "",
            totalLessonsInModule = 0,
            lessonsCompleted = 0,
            streakDays = 0,
            dailyGoalsDone = 0,
            dailyGoalsTotal = 0,
            weeklyGoalsDone = 0,
            weeklyGoalsTotal = 0
        )
    )
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        refresh()
    }

    private suspend fun buildState(): HomeUiState {
        val user = model.getUser()
        val progressSummary = model.getProgressSummary()
        val learningProgress = model.getUserLearningProgress()

        val module = model.getModule(learningProgress.moduleId)
        val lessons = model.getLessonsByModuleId(module.moduleId).sortedBy { it.lessonId }
        val totalLessons = lessons.size

        val lessonsCompleted = when {
            learningProgress.lessonId == -1 -> totalLessons
            else -> {
                val currentLessonIndex = lessons.indexOfFirst { it.lessonId == learningProgress.lessonId }
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
        scope.launch {
            state = buildState()
        }
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