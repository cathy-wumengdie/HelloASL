package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ca.uwaterloo.helloasl.ui.navigations.ProfileDestination
import ca.uwaterloo.helloasl.ui.navigations.ProfileNavEvent
import kotlinx.coroutines.*

class ProfileViewModel(private val model: Model) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var state by mutableStateOf(
        ProfileUiState(
            userName = "",
            avatarText = "",
            wordsLearned = 0,
            starredSigns = 0,
            learningGoalPerDay = 0,
            learningGoalPerWeek = 0
        )
    )
        private set

    init {
        refresh()
    }

    private suspend fun buildState(): ProfileUiState {
        val user = model.getUser()
        val progressSummary = model.getProgressSummary()
        val learningProgress = model.getUserLearningProgress()
        return ProfileUiState(
            userName = user.name,
            avatarText = user.avatarText,
            wordsLearned = learningProgress.wordsLearned,
            starredSigns = 0,       /* later after star implemented*/
            learningGoalPerDay = progressSummary.dailyProgress.dailyGoalMinutes,
            learningGoalPerWeek = progressSummary.weeklyProgress.weeklyGoalDays,
        )
    }

    fun refresh() {
        scope.launch {
            state = buildState()
        }
    }

    fun onSaveLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        scope.launch {
            model.setLearningGoals(minutesPerDay, daysPerWeek)
            state = buildState()
        }
    }

    private val _navEvents = MutableSharedFlow<ProfileNavEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents: SharedFlow<ProfileNavEvent> = _navEvents.asSharedFlow()

    fun onSettings() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.SETTINGS))
    }

    fun onWordsLearned() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.WORDS_LEARNED))
    }

    fun onStarredSigns() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.STARRED_SIGNS))
    }

    fun onAccount() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.ACCOUNT))
    }

    fun onLicense() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.LICENSE))
    }

    suspend fun onSignOut() {
        val result = model.logout()
        result.onSuccess {
            _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.SIGN_IN))
        }.onFailure { e ->
            println("Logout failed: ${e.message}")
        }
    }
}