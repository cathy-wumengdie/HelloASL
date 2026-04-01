package ca.uwaterloo.helloasl.data.translateRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult
import ca.uwaterloo.helloasl.domain.translateModel.TranslateHistoryItem
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign

class MockTranslateRepository(private val db: MockDB) : TranslateRepository {
    override suspend fun searchWord(word: String): ASLSign? = db.searchWord(word)

    override suspend fun getSearchHistory(): List<TranslateHistoryItem> = db.getTranslateSearchHistory()

    override suspend fun addHistory(word: String) = db.addTranslateHistory(word)

    override suspend fun clearHistory() = db.clearTranslateHistory()

    override suspend fun recognizeAslFromVideo(videoUri: String): AslRecognitionResult = db.recognizeAsl()
}