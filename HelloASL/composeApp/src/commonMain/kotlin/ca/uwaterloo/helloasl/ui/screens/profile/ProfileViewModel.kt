package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class ProfileDestination {
    SETTINGS,
    WORDS_LEARNED,
    STARRED_SIGNS,
    SET_LEARNING_GOALS,
    ACCOUNT,
    LICENSE,
    SIGN_IN
}

data class ProfileNavEvent(val dest: ProfileDestination)

class ProfileViewModel {
    var state by mutableStateOf(ProfileModel())
        private set

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

    fun onSetLearningGoals() {
        _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.SET_LEARNING_GOALS))
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