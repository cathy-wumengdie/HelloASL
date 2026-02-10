package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TranslateViewModel {
    var state by mutableStateOf(TranslateState())
        private set

    fun onSwitchMode(mode: TranslateMode) {
        state = state.copy(mode = mode)
    }

    fun onSearch() { }
    fun onSelectHistoryItem() { }
    fun onStartCamera() { }
}