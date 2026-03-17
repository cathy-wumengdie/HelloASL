package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TranslateViewModel(private val model: Model) {
    var state by mutableStateOf(TranslateUiState())
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            state = state.copy(
                searchHistory = model.getTranslateHistory()
            )
        }
    }

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

        scope.launch {
            val result = model.translateWord(q)

            if (result != null) {
                model.addTranslateHistory(q)
            }

            state = state.copy(
                searchHistory = model.getTranslateHistory(),
                lastResult = result,
                errorMessage = if (result == null) "No result found." else null
            )
        }
    }

    fun onSelectHistoryItem(word: String) {
        state = state.copy(query = word, errorMessage = null)
        onSearch()
    }

    fun onStartCamera() {
        state = state.copy(isCameraRunning = true, errorMessage = null)

        scope.launch {
            val reco = model.recognizeAsl()
            state = state.copy(
                recoText = reco.recognizedText,
                confidence = reco.confidence
            )
        }
    }

    fun onStopCamera() {
        state = state.copy(isCameraRunning = false, errorMessage = null)
    }

    fun onClearHistory() {
        scope.launch {
            model.clearTranslateHistory()
            state = state.copy(searchHistory = emptyList(), errorMessage = null)
        }
    }
}