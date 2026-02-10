package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TranslateViewModel {
    var state by mutableStateOf(TranslateUiState())
        private set

    fun onSwitchMode(mode: TranslateMode) {
        state = state.copy(mode = mode)
    }

    fun onQueryChange(newQuery: String) {
        state = state.copy(query = newQuery)
    }

    fun onSearch() { }
    fun onSelectHistoryItem(word: String) { }
    fun onStartCamera() { }
}