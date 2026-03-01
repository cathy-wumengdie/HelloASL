package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.String

enum class ProfileDestination {
    SETTINGS,
    WORDS_LEARNED,
    STARRED_SIGNS,
    ACCOUNT,
    LICENSE,
    SIGN_IN
}

data class ProfileNavEvent(val dest: ProfileDestination)

class ProfileViewModel ( private val model: Model) {
    var state by mutableStateOf(buildState())
        private set

    private fun buildState(): ProfileUiState {
        val user = model.getUser()
        val profile = model.getUserProfile()
        return ProfileUiState(
            userName = user.name,
            avatarText = user.avatarText,
            wordsLearned = profile.wordsLearned,
            starredSigns = profile.starredSigns,
            learningGoalPerDay = profile.learningGoalPerDay,
            learningGoalPerWeek = profile.learningGoalPerWeek
        )
    }

    fun refresh() {
        state = buildState()
    }

    fun onSaveLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
        model.setLearningGoals(minutesPerDay, daysPerWeek)
        refresh()
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

    fun onSignOut() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.SIGN_IN))
    }
}