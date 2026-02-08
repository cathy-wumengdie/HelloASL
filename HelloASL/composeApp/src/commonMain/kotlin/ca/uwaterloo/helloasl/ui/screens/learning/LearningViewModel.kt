package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LearningViewModel {
    var state by mutableStateOf(LearningState())
        private set

    fun onOpenStarred() { }
    fun onOpenAlphabet(range: String) { }
    fun onOpenGreetings(topic: String) { }
}