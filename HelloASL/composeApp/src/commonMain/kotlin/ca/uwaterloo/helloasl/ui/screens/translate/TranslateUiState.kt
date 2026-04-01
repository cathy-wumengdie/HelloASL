package ca.uwaterloo.helloasl.ui.screens.translate

import ca.uwaterloo.helloasl.domain.translateModel.TranslateHistoryItem
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign

enum class TranslateMode { EN_TO_ASL, ASL_TO_EN }

data class TranslateUiState(
    val mode: TranslateMode = TranslateMode.EN_TO_ASL,

    // EN -> ASL
    val queryHint: String = "Search an English word",
    val query: String = "",
    val searchHistory: List<TranslateHistoryItem> = emptyList(),
    val lastResult: ASLSign? = null,

    // ASL -> EN
    val recoText: String = "",
    val confidence: Float = 0f,
    val errorMessage: String? = null,
    val isPreviewActive: Boolean = false,
    val isRecording: Boolean = false,
    val recordedVideoUri: String? = null,
    val isRecognizing: Boolean = false
) {
    val confidenceLabel: String
        get() = "${(confidence * 100).toInt()}%"
}