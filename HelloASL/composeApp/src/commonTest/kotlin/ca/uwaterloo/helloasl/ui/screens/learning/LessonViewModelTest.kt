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
    fun loadLessonPopulatesOptionsAndProgress() {
        val (vm, model) = newVm()
        vm.loadLesson(1)
        val state = vm.state
        assertEquals("Basic Greetings", state.title)
        assertEquals(2, state.options.size)
        assertEquals("1/2", state.progress)
    }

    @Test
    fun correctAnswerShowsNextWhenNotLast() {
        val (vm, model) = newVm()
        vm.loadLesson(1)
        val correct = model.getSignsForLesson(1).first().word
        vm.onChoose(correct)
        assertTrue(vm.state.showNext)
    }

    @Test
    fun lastQuestionCorrectFiresCompletionAndHidesNext() {
        val (vm, model) = newVm()
        vm.loadLesson(1)
        var completedId: Int? = null
        vm.setOnLessonCompleted { completedId = it }
        vm.onNext()
        val correct = model.getSignsForLesson(1)[1].word
        vm.onChoose(correct)
        assertEquals(1, completedId)
        assertFalse(vm.state.showNext)
    }

    @Test
    fun incorrectAnswerDoesNotShowNextAndAllowsRetry() {
        val (vm, model) = newVm()
        vm.loadLesson(1)
        val signs = model.getSignsForLesson(1)
        val correct = signs.first().word
        val wrong = signs.last().word // different sign
        vm.onChoose(wrong)
        assertFalse(vm.state.showNext)
        assertFalse(vm.state.isCorrect ?: true)
        vm.onChoose(correct)
        assertTrue(vm.state.isCorrect ?: false)
    }

    @Test
    fun onNextStopsAtLastQuestion() {
        val (vm, _) = newVm()
        vm.loadLesson(1)
        vm.onNext() // move to second question
        val progressAfterOne = vm.state.progress
        vm.onNext() // should stay on last
        assertEquals(progressAfterOne, vm.state.progress)
    }
}
