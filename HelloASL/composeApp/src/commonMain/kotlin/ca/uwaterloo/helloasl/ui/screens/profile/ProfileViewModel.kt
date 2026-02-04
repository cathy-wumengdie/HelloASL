package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProfileViewModel {
    var state by mutableStateOf(ProfileState())
        private set

    fun onContinueLearning() { }
    fun onDayStreak() { }
    fun onDailyGoals() { }
    fun onLearnAsl() { }
    fun onTakeQuiz() { }
    fun onTranslate() { }
    fun onNotifications() { }
}