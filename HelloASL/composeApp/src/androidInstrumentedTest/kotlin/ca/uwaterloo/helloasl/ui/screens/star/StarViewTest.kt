package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.ui.test.assertCountEquals
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
class StarViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createDependencies(): Pair<StarViewModel, Model> {
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
        val vm = StarViewModel(model)
        return vm to model
    }

    @Test
    fun starView_rendersTagsAndItems() {
        val (vm, _) = createDependencies()

        composeTestRule.setContent {
            StarView(vm = vm)
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("All").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNode(hasText("All") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Favorites") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Practice") and hasClickAction()).assertExists()
        composeTestRule.onNodeWithText("Hello").assertExists()
        composeTestRule.onNodeWithText("Thanks").assertExists()
        composeTestRule.onAllNodesWithText("Remove").assertCountEquals(2)
    }
}
