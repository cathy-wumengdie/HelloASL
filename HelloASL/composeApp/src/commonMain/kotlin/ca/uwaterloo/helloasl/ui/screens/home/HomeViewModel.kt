package ca.uwaterloo.helloasl.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class HomeViewModel {
    var state by mutableStateOf(HomeState())
        private set

    fun onContinueLearning() { }
    fun onDayStreak() { }
    fun onDailyGoals() { }
    fun onLearnAsl() { }
    fun onTakeQuiz() { }
    fun onTranslate() { }
    fun onNotifications() { }
}