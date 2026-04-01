package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class TranslateModelTest {
    private fun makeModel(): Model {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db),
            notification = NoOpNotificationRepository
        )
        return Model(repos)
    }

    @Test
    fun addTranslateHistory_then_getTranslateHistory_returns_newest_first() = runTest {
        val model = makeModel()

        model.clearTranslateHistory()
        model.addTranslateHistory("Hello")
        model.addTranslateHistory("Thanks")

        val history = model.getTranslateHistory()
        assertTrue(history.isNotEmpty())
        assertEquals("Thanks", history.first().query)
    }

    @Test
    fun translateWord_returns_asl_sign() = runTest {
        val model = makeModel()

        val result = model.translateWord("hello")
        assertNotNull(result)
        assertEquals("Hello", result.gloss)
        assertTrue(result.videoUrl1.isNotBlank())
    }

    @Test
    fun recognizeAslFromVideo_returns_text_and_confidence() = runTest {
        val model = makeModel()

        val reco = model.recognizeAslFromVideo("file://demo.mp4")
        assertTrue(reco.recognizedText.isNotBlank())
        assertTrue(reco.confidence in 0f..1f)
    }
}