package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.translateModel.TranslateResult

class TranslateViewModel(private val model: Model) {

    var state by mutableStateOf(
        TranslateUiState(
            searchHistory = model.getTranslateHistory()
        )
    )
        private set

    fun onSwitchMode(mode: TranslateMode) {
        state = state.copy(mode = mode, errorMessage = null)
    }

    fun onQueryChange(newQuery: String) {
        state = state.copy(query = newQuery, errorMessage = null)
    }

    fun onSearch() {
        val q = state.query.trim()
        if (q.isBlank()) {
            state = state.copy(errorMessage = "Please enter a word.")
            return
        }

        model.addTranslateHistory(q)
        val result: TranslateResult? = model.translateWord(q)

        state = state.copy(
            searchHistory = model.getTranslateHistory(),
            lastResult = result,
            errorMessage = null
        )
    }

    fun onSelectHistoryItem(word: String) {
        state = state.copy(query = word, errorMessage = null)
        onSearch()  // if you click on a word in the history, it will search the word for you
    }

    // Sprint 2: turn camera preview on
    fun onStartCamera() {
        state = state.copy(isCameraRunning = true, errorMessage = null)

        val reco = model.recognizeAsl()
        state = state.copy(
            recoText = reco.recognizedText,
            confidence = reco.confidence
        )
    }

    // Sprint 2: turn camera preview off
    fun onStopCamera() {
        state = state.copy(isCameraRunning = false, errorMessage = null)
    }

    // For future use
    fun onClearHistory() {
        model.clearTranslateHistory()
        state = state.copy(searchHistory = emptyList(), errorMessage = null)
    }
}