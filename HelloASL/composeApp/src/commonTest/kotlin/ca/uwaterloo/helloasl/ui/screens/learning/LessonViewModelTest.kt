package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class LessonViewModelTest {
    private fun newVm(): Pair<LessonViewModel, Model> {
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
        return LessonViewModel(model) to model
    }

    @Test
    fun loadLessonShowsViewingState() = runBlocking {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        delay(50)
        val state = vm.state
        assertEquals("Basic Greetings", state.title)
        assertEquals(LessonPhase.VIEWING, state.phase)
        assertEquals(0, state.options.size)
        assertEquals("Sign 1/2", state.progress)
    }

    @Test
    fun startQuizPopulatesOptionsAndProgress() = runBlocking {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        delay(50)
        vm.onStartQuiz()
        val state = vm.state
        assertEquals(LessonPhase.QUIZ, state.phase)
        assertEquals(3, state.options.size)
        assertEquals("Quiz 1/2", state.progress)
    }

    @Test
    fun correctAnswerShowsNextWhenNotLast() = runBlocking {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        delay(50)
        vm.onStartQuiz()
        vm.onChoose("Hello")
        assertTrue(vm.state.showNext)
    }

    @Test
    fun lastQuestionCorrectFiresCompletionAndHidesNext() = runBlocking {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        delay(50)
        var completedId: Int? = null
        vm.setOnLessonCompleted { completedId = it }
        vm.onStartQuiz()
        vm.onChoose("Hello")
        vm.onNext()
        vm.onChoose("Thanks")
        assertEquals(1, completedId)
        assertFalse(vm.state.showNext)
    }

    @Test
    fun incorrectAnswerDoesNotShowNextAndAllowsRetry() = runBlocking {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        delay(50)
        vm.onStartQuiz()
        vm.onChoose("Yes")
        assertFalse(vm.state.showNext)
        assertFalse(vm.state.isCorrect ?: true)
        vm.onChoose("Hello")
        assertTrue(vm.state.isCorrect ?: false)
    }

    @Test
    fun onNextStopsAtLastQuestion() = runBlocking {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        delay(50)
        vm.onStartQuiz()
        vm.onChoose("Hello")
        vm.onNext() // move to second question
        val progressAfterOne = vm.state.progress
        vm.onNext() // should stay on last
        assertEquals(progressAfterOne, vm.state.progress)
    }
}
