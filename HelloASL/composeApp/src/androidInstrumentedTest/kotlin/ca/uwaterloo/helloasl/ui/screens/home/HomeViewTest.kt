package ca.uwaterloo.helloasl.ui.screens.home

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
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
@RunWith(AndroidJUnit4::class)
class HomeViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private fun createDependencies(): Pair<Model, HomeViewModel> {
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
                dailyProgress = DailyProgress(
                    minutesLearned = 10,
                    lastDailyGoalCompletedDate = null,
                    dailyGoalMinutes = 15
                ),
                weeklyProgress = WeeklyProgress(
                    daysCompleted = 2,
                    lastCreditedDate = null,
                    weeklyGoalDays = 5
                ),
                dayStreak = 3
            )
        )
        kotlinx.coroutines.runBlocking {
            model.login("yanjin@gmail.com", "1234")
        }
        val vm = HomeViewModel(model, kotlinx.coroutines.CoroutineScope(Dispatchers.Unconfined))
        return Pair(model, vm)
    }
    @Test
    fun homeView_rendersCorrectProgress() {
        val (_, vm) = createDependencies()
        composeTestRule.setContent {
            HomeView(vm = vm)
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("10 / 15 min today").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Your Progress").assertExists()
        composeTestRule.onNodeWithText("Module 1: Basics").assertExists()
        composeTestRule.onNodeWithText("Continue Learning").assertExists()
        composeTestRule.onNodeWithText("3 Day Streak").assertExists()
        composeTestRule.onNodeWithText("10 / 15 min today").assertExists()
        composeTestRule.onNodeWithText("2 / 5 days/week").assertExists()
        composeTestRule.onNodeWithText("Learn ASL").assertExists()
        composeTestRule.onNodeWithText("English <-> ASL").assertExists()
    }
}
