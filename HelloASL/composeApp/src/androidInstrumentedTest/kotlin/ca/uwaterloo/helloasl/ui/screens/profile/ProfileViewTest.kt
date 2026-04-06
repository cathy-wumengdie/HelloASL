package ca.uwaterloo.helloasl.ui.screens.profile

import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class ProfileViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private fun createDependencies(): Pair<Model, ProfileViewModel> {
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
        db.putProgressSummary(
            "1",
            ProgressSummary(
                userId = "1",
                date = today(),
                dailyProgress = DailyProgress(0, null, 15),
                weeklyProgress = WeeklyProgress(0, null, 5),
                dayStreak = 0
            )
        )
        kotlinx.coroutines.runBlocking {
            model.login("yanjin@gmail.com", "1234")
        }
        val vm = ProfileViewModel(model, kotlinx.coroutines.CoroutineScope(Dispatchers.Unconfined))
        return Pair(model, vm)
    }
    @Test
    fun profileView_rendersUserDataAndStats() {
        val (_, vm) = createDependencies()
        composeTestRule.setContent {
            ProfileView(vm = vm)
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Yanjin").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Yanjin").assertExists()
        composeTestRule.onNodeWithText("Words Learned").assertExists()
        composeTestRule.onNodeWithText("Starred Signs").assertExists()
        composeTestRule.onNodeWithText("Account").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("Sign out").assertExists()
    }
}
