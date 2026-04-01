package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.MainDispatcherRule
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LessonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun newVm(): Pair<LessonViewModel, Model> {
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
        val model = Model(repos, ioDispatcher = mainDispatcherRule.dispatcher)
        model.login("yanjin@gmail.com", "1234")
        return LessonViewModel(model) to model
    }

    private suspend fun kotlinx.coroutines.test.TestScope.awaitPhase(
        vm: LessonViewModel,
        phase: LessonPhase
    ) {
        repeat(5) {
            advanceUntilIdle()
            if (vm.state.phase == phase) return
        }
    }

    @Test
    fun loadLessonShowsViewingState() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        val state = vm.state
        assertEquals("Basic Greetings", state.title)
        assertEquals(LessonPhase.VIEWING, state.phase)
        assertEquals(0, state.options.size)
        assertEquals("Sign 1/2", state.progress)
    }

    @Test
    fun startQuizPopulatesOptionsAndProgress() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        vm.onStartQuiz()
        advanceUntilIdle()
        val state = vm.state
        assertEquals(LessonPhase.QUIZ, state.phase)
        assertEquals(3, state.options.size)
        assertEquals("Quiz 1/2", state.progress)
    }

    @Test
    fun correctAnswerShowsNextWhenNotLast() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        vm.onStartQuiz()
        advanceUntilIdle()
        vm.onChoose("Hello")
        assertTrue(vm.state.showNext)
    }

    @Test
    fun lastQuestionCorrectFiresCompletionAndHidesNext() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        var completedId: Long? = null
        vm.setOnLessonCompleted { completedId = it }
        vm.onStartQuiz()
        advanceUntilIdle()
        vm.onChoose("Hello")
        vm.onNext()
        advanceUntilIdle()
        vm.onChoose("Thanks")
        advanceUntilIdle()
        assertEquals(1L, completedId)
        assertFalse(vm.state.showNext)
    }

    @Test
    fun incorrectAnswerDoesNotShowNextAndAllowsRetry() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        vm.onStartQuiz()
        advanceUntilIdle()
        vm.onChoose("Yes")
        assertFalse(vm.state.showNext)
        assertFalse(vm.state.isCorrect ?: true)
        vm.onChoose("Hello")
        assertTrue(vm.state.isCorrect ?: false)
    }

    @Test
    fun onNextStopsAtLastQuestion() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        vm.onStartQuiz()
        advanceUntilIdle()
        vm.onChoose("Hello")
        vm.onNext()
        advanceUntilIdle()
        val progressAfterOne = vm.state.progress
        vm.onNext()
        assertEquals(progressAfterOne, vm.state.progress)
    }

    @Test
    fun onNextSignUpdatesViewingProgress() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        val progressBefore = vm.state.progress
        vm.onNextSign()
        assertTrue(vm.state.progress != progressBefore)
    }

    @Test
    fun onNextVideoUsesAltUrlWhenPresent() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        val initialUrl = vm.state.videoUrl
        vm.onNextVideo()
        assertEquals(initialUrl, vm.state.videoUrl)
    }

    @Test
    fun startQuizHidesStartButton() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        vm.onNextSign()
        advanceUntilIdle()
        assertTrue(vm.state.showStartQuiz)
        vm.onStartQuiz()
        advanceUntilIdle()
        assertFalse(vm.state.showStartQuiz)
    }

    @Test
    fun chooseSetsSelectedOptionEvenWhenIncorrect() = runTest {
        val (vm, _) = newVm()
        vm.loadLesson(1L)
        awaitPhase(vm, LessonPhase.VIEWING)
        vm.onStartQuiz()
        advanceUntilIdle()
        vm.onChoose("Yes")
        assertEquals("Yes", vm.state.selected)
        assertFalse(vm.state.isCorrect ?: true)
    }
}