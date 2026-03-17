package ca.uwaterloo.helloasl.ui.screens.learning

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class LearningViewModelTest {
    private fun newVm(): Pair<LearningViewModel, Model> {
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
        return LearningViewModel(model) to model
    }

    @Test
    fun unlockNextUnlocksFollowingLesson() = runBlocking {
        val (vm, model) = newVm()
        vm.refresh()
        delay(50)
        assertTrue(model.isLessonLocked(2))
        vm.unlockNext(completedLessonId = 1)
        assertTrue(!model.isLessonLocked(2))
    }
}
