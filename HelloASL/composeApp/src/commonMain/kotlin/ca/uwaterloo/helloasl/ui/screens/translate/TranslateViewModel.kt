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

    fun onStartPreview() {
        state = state.copy(
            isPreviewActive = true,
            errorMessage = null
        )
    }

    fun onStopPreview() {
        state = state.copy(
            isPreviewActive = false,
            isRecording = false,
            errorMessage = null
        )
    }

    fun onStartRecording() {
        if (!state.isPreviewActive) {
            state = state.copy(errorMessage = "Start the camera first.")
            return
        }

        state = state.copy(
            isRecording = true,
            recoText = "",
            confidence = 0f,
            errorMessage = null
        )
    }

    fun onStopRecording() {
        state = state.copy(
            isRecording = false,
            errorMessage = null
        )
    }

    fun onRecordingSaved(videoUri: String) {
        state = state.copy(
            isRecording = false,
            recordedVideoUri = videoUri,
            errorMessage = null
        )
    }

    fun onRecordingError(message: String) {
        state = state.copy(
            isRecording = false,
            errorMessage = message
        )
    }

    fun onClearRecording() {
        state = state.copy(
            recordedVideoUri = null,
            recoText = "",
            confidence = 0f,
            isRecognizing = false,
            errorMessage = null
        )
    }

    fun onInterpretRecording() {
        val uri = state.recordedVideoUri
        if (uri.isNullOrBlank()) {
            state = state.copy(errorMessage = "Please record a video first.")
            return
        }

        state = state.copy(
            isRecognizing = true,
            errorMessage = null
        )

        scope.launch {
            try {
                val reco = model.recognizeAslFromVideo(uri)
                state = state.copy(
                    recoText = reco.recognizedText,
                    confidence = reco.confidence,
                    isRecognizing = false
                )
            } catch (e: Exception) {
                state = state.copy(
                    isRecognizing = false,
                    errorMessage = e.message ?: "Failed to interpret recording."
                )
            }
        }
    }

    fun onClearHistory() {
        scope.launch {
            model.clearTranslateHistory()
            state = state.copy(searchHistory = emptyList(), errorMessage = null)
        }
    }
}