package ca.uwaterloo.helloasl.domain

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LearningModelTest {
    private suspend fun newLoggedInModel(): Model {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        return Model(repos).also {
            it.login("yanjin@gmail.com", "1234")
        }
    }

    @Test
    fun `unlock next lesson reflects in lock state`() = runBlocking {
        val model = newLoggedInModel()
        model.prepareLessonLocks()
        assertTrue(model.isLessonLocked(2))
        model.unlockLesson(2)
        assertFalse(model.isLessonLocked(2))
    }

    @Test
    fun `getSignsForLesson returns correct sign count`() = runBlocking {
        val model = newLoggedInModel()
        val signs = model.getSignsForLesson(1)
        assertEquals(2, signs.size)
        assertEquals("Hello", signs.first().gloss)
    }

    @Test
    fun `toggleStar toggles starred state`() = runBlocking {
        val model = newLoggedInModel()
        val signId = 1L
        val tagId = 1L
        assertFalse(model.isStarred(signId))
        assertTrue(model.toggleStar(signId, tagId))
        assertTrue(model.isStarred(signId))
        assertFalse(model.toggleStar(signId, tagId))
        assertFalse(model.isStarred(signId))
    }

    @Test
    fun `prepareLessonLocks unlocks only first lesson when none completed`() = runBlocking {
        val model = newLoggedInModel()
        model.prepareLessonLocks()
        assertFalse(model.isLessonLocked(1))
        assertTrue(model.isLessonLocked(2))
    }

    @Test
    fun `prepareLessonLocks unlocks next lesson after max completed`() = runBlocking {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        val model = Model(repos)
        model.login("erdolong@gmail.com", "abc")
        model.prepareLessonLocks()
        assertFalse(model.isLessonLocked(2))
    }

    @Test
    fun `getQuizChoicesForSigns returns empty for empty input`() = runBlocking {
        val model = newLoggedInModel()
        val choices = model.getQuizChoicesForSigns(emptyList())
        assertTrue(choices.isEmpty())
    }

    @Test
    fun `getSignCountForLesson returns expected count`() = runBlocking {
        val model = newLoggedInModel()
        assertEquals(2, model.getSignCountForLesson(1))
    }
}