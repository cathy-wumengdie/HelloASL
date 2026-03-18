package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.MainDispatcherRule
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LearningViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun newVm(): Pair<LearningViewModel, Model> {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        val model = Model(repos, ioDispatcher = mainDispatcherRule.dispatcher)
        model.login("yanjin@gmail.com", "1234")
        model.prepareLessonLocks()
        return LearningViewModel(model) to model
    }

    @Test
    fun unlockNextUnlocksFollowingLesson() = runTest {
        val (vm, model) = newVm()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(model.isLessonLocked(2))
        vm.unlockNext(completedLessonId = 1)
        advanceUntilIdle()
        assertFalse(model.isLessonLocked(2))
    }

    @Test
    fun refreshBuildsLessonItems() = runTest {
        val (vm, _) = newVm()
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.state.lessonItems.isNotEmpty())
    }

    @Test
    fun unlockNextDoesNothingAtLastLesson() = runTest {
        val (vm, model) = newVm()
        vm.refresh()
        advanceUntilIdle()
        val wasLocked = model.isLessonLocked(2)
        vm.unlockNext(completedLessonId = 2)
        advanceUntilIdle()
        assertTrue(model.isLessonLocked(2) == wasLocked)
    }
}