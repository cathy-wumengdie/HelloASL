package ca.uwaterloo.helloasl.data.translateRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult
import ca.uwaterloo.helloasl.domain.translateModel.TranslateHistoryItem
import ca.uwaterloo.helloasl.domain.translateModel.TranslateResult

class MockTranslateRepository(private val db: MockDB) : TranslateRepository {
    override fun searchWord(word: String): TranslateResult? = db.searchWord(word)

    override fun getSearchHistory(): List<TranslateHistoryItem> = db.getTranslateSearchHistory()

    override fun addHistory(word: String) = db.addTranslateHistory(word)

    override fun clearHistory() = db.clearTranslateHistory()

    override fun recognizeAsl(): AslRecognitionResult = db.recognizeAsl()
}