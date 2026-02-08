package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LessonViewModel {
    var state by mutableStateOf(LessonState())
        public set

    fun onChoose(option: String) { }
    fun onSkip() { }
    fun onStar() { }
}
