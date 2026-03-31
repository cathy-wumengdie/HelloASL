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

class ProfileViewModel(private val model: Model, private val scope: CoroutineScope) {
    var state by mutableStateOf(
        ProfileUiState(
            userName = "",
            avatarText = "",
            wordsLearned = 0,
            starredSigns = 0,
            learningGoalPerDay = 0,
            learningGoalPerWeek = 0,
            email = "",
        )
    )
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var isPasswordLoading by mutableStateOf(false)
        private set

    var passwordSuccess by mutableStateOf(false)
        private set

    var nameError by mutableStateOf<String?>(null)
        private set

    var nameSuccess by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    private suspend fun buildState(): ProfileUiState {
        val user = model.getUser()
        val progressSummary = model.getProgressSummary()
        val learningProgress = model.getUserLearningProgress()
        val starredItems = model.getStarredItems()
        return ProfileUiState(
            userName = user.name,
            avatarText = user.avatarText,
            wordsLearned = learningProgress.wordsLearned,
            starredSigns = starredItems.size,
            learningGoalPerDay = progressSummary.dailyProgress.dailyGoalMinutes,
            learningGoalPerWeek = progressSummary.weeklyProgress.weeklyGoalDays,
            email = user.email
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

    suspend fun onSignOut() {
        val result = model.logout()
        result.onSuccess {
            _navEvents.tryEmit(ProfileNavEvent(ProfileDestination.SIGN_IN))
        }.onFailure { e ->
            println("Logout failed: ${e.message}")
        }
    }

    fun onEditName(newName: String) {
        scope.launch {
            nameError = null
            nameSuccess = false

            val result = model.updateName(newName)

            result.onSuccess {
                nameSuccess = true
            }.onFailure { e ->
                nameError = e.message ?: "Failed to update name"
            }
        }
    }

    fun onEditPassword(current: String, new: String) {
        scope.launch {
            val user = model.getUser()

            isPasswordLoading = true
            passwordError = null
            passwordSuccess = false

            val result = model.changePassword(
                email = user.email,
                currentPassword = current,
                newPassword = new
            )

            isPasswordLoading = false

            result.onSuccess {
                passwordSuccess = true
            }.onFailure { e ->
                passwordError = mapError(e)
            }
        }
    }

    private fun mapError(e: Throwable): String {
        val msg = e.message ?: ""

        return when {
            msg.contains("Invalid login", ignoreCase = true) ->
                "Current password is incorrect"

            msg.contains("timeout", ignoreCase = true) ->
                "Request timed out, please try again"

            msg.contains("Unable to resolve host", ignoreCase = true) ->
                "No internet connection"

            msg.contains("weak_password", ignoreCase = true) ->
                "Password must be at least 6 characters"

            msg.contains("Current password incorrect", ignoreCase = true) ->
                "Current password incorrect"

            else -> {
                println("Password update failed: ${e.message}")
                "Failed to update password"
            }
        }
    }
}