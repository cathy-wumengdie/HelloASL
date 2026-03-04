package ca.uwaterloo.helloasl.data.translateRepository

import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult
import ca.uwaterloo.helloasl.domain.translateModel.TranslateHistoryItem
import ca.uwaterloo.helloasl.domain.translateModel.TranslateResult

interface TranslateRepository {
    fun searchWord(word: String): TranslateResult?
    fun getSearchHistory(): List<TranslateHistoryItem>
    fun addHistory(word: String)
    fun clearHistory()
    fun recognizeAsl(): AslRecognitionResult  // For Sprint 2 only, no AI yet
}