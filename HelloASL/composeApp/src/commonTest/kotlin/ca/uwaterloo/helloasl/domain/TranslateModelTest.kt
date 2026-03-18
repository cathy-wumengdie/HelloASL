package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import kotlin.test.*

internal class TranslateModelTest {
    private fun makeModel(): Pair<MockDB, Model> {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        return db to Model(repos)
    }

    @Test
    fun addTranslateHistory_then_getTranslateHistory_returns_newest_first() {
        val (db, model) = makeModel()

        model.clearTranslateHistory()
        model.addTranslateHistory("Hello")
        model.addTranslateHistory("Thanks")

        val history = model.getTranslateHistory()
        assertTrue(history.isNotEmpty())
        assertEquals("Thanks", history.first().query)
    }

    @Test
    fun translateWord_returns_result_object() {
        val (db, model) = makeModel()

        val result = model.translateWord("hello")
        assertNotNull(result)
        assertTrue(result.query.isNotBlank())
    }

    @Test
    fun recognizeAsl_returns_text_and_confidence() {
        val (db, model) = makeModel()

        val reco = model.recognizeAsl()
        assertTrue(reco.recognizedText.isNotBlank())
        assertTrue(reco.confidence in 0f..1f)
    }
}