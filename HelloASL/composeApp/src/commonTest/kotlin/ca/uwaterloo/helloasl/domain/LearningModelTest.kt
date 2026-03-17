package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class LearningModelTest {
    private fun newModel(): Model {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        val model = Model(repos)
        model.login("yanjin@gmail.com", "1234")
        return model
    }

    @Test
    fun `unlockNext lesson reflects in lock state`() = runBlocking {
        val model = newModel()
        assertTrue(model.isLessonLocked(2))
        model.unlockLesson(2)
        assertFalse(model.isLessonLocked(2))
    }

    @Test
    fun `getSignsForLesson returns correct sign count`() = runBlocking {
        val model = newModel()
        val signs = model.getSignsForLesson(1)
        assertEquals(2, signs.size)
        assertEquals("Hello", signs.first().gloss)
    }

    @Test
    fun `toggleStar toggles starred state`() = runBlocking {
        val model = newModel()
        val signId = 1
        assertFalse(model.isStarred(signId))
        assertTrue(model.toggleStar(signId))
        assertTrue(model.isStarred(signId))
        assertFalse(model.toggleStar(signId))
        assertFalse(model.isStarred(signId))
    }
}
