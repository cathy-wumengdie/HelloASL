package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.ui.navigations.HomeDestination
import ca.uwaterloo.helloasl.ui.navigations.HomeNavEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val model: Model) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            state = buildState()
        }
    }

    private suspend fun buildState(): HomeUiState {
        val user = model.getUser()
        val progressSummary = model.getProgressSummary()
        val learningProgress = model.getUserLearningProgress()

        val moduleId = learningProgress.moduleId
        val lessonId = learningProgress.lessonId

        val module = moduleId?.let { model.getModule(it) }

        val lessons = moduleId
            ?.let { model.getLessonsByModuleId(it).sortedBy { l -> l.lessonId } }
            ?: emptyList()

        val totalLessons = lessons.size

        val lessonsCompleted =
            if (lessonId == null) {
                totalLessons
            } else {
                val index = lessons.indexOfFirst { it.lessonId == lessonId }
                if (index == -1) totalLessons else index
            }

        return HomeUiState(
            userName = user.name,
            moduleTitle = module?.title ?: "Learning",
            totalLessonsInModule = totalLessons,
            lessonsCompleted = lessonsCompleted,
            streakDays = progressSummary.dayStreak,
            dailyGoalsDone = progressSummary.dailyProgress.minutesLearned,
            dailyGoalsTotal = progressSummary.dailyProgress.dailyGoalMinutes,
            weeklyGoalsDone = progressSummary.weeklyProgress.daysCompleted,
            weeklyGoalsTotal = progressSummary.weeklyProgress.weeklyGoalDays
        )
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