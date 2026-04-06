package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createDependencies(): Pair<LearningViewModel, Model> {
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
        val model = Model(repos, Dispatchers.Unconfined)
        kotlinx.coroutines.runBlocking {
            model.login("yanjin@gmail.com", "1234")
        }
        val vm = LearningViewModel(model)
        return vm to model
    }

    @Test
    fun learningView_rendersModulesAndLessons() {
        val (vm, _) = createDependencies()

        composeTestRule.setContent {
            LearningView(
                onOpenLesson = {},
                onOpenStarred = {},
                vm = vm
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Basics").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Starred").assertExists()
        composeTestRule.onNodeWithText("Basics").assertExists()
        composeTestRule.onNodeWithText("Basic Greetings").assertExists()
        composeTestRule.onNodeWithText("Yes / No").assertExists()
    }

    @Test
    fun learningView_starredRowIsClickable() {
        val (vm, _) = createDependencies()

        composeTestRule.setContent {
            LearningView(
                onOpenLesson = {},
                onOpenStarred = {},
                vm = vm
            )
        }

        composeTestRule.onNode(hasText("Starred") and hasClickAction()).assertExists()
    }
}
