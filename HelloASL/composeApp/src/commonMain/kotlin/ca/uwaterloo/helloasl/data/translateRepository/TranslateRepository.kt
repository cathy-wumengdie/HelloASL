package ca.uwaterloo.helloasl.data.translateRepository

import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult
import ca.uwaterloo.helloasl.domain.translateModel.TranslateHistoryItem
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign

interface TranslateRepository {
    suspend fun searchWord(word: String): ASLSign?
    suspend fun getSearchHistory(): List<TranslateHistoryItem>
    suspend fun addHistory(word: String)
    suspend fun clearHistory()
    suspend fun recognizeAslFromVideo(videoUri: String): AslRecognitionResult
}