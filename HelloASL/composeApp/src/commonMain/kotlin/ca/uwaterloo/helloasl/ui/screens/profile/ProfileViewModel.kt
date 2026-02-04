package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProfileViewModel {
    var state by mutableStateOf(ProfileState())
        private set

    fun onSettings() { }
    fun onWordsLearned() { }
    fun onStarredSigns() { }
    fun onSetLearningGoals() { }
    fun onAccount() { }
    fun onLicense() { }
    fun onSignOut() { }
}