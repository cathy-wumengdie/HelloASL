package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import org.junit.Rule
import org.junit.Test

class TranslateViewAndroidTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun makeVm(): TranslateViewModel {
        val db = MockDB()
        val model = Model(
            Repositories(
                auth = MockAuthRepository(db),
                user = MockUserRepository(db),
                star = MockStarRepository(db),
                learning = MockLearningRepository(db),
                translate = MockTranslateRepository(db),
                progressTracker = MockProgressTrackerRepository(db),
                notification = NoOpNotificationRepository
            )
        )
        return TranslateViewModel(model)
    }

    @Test
    fun translateView_startsInEnglishToAslMode() {
        val vm = makeVm()

        composeRule.setContent {
            MaterialTheme {
                TranslateView(
                    vm = vm,
                    hasCameraHardware = true,
                    cameraGranted = true,
                    requestCameraPermission = {}
                )
            }
        }

        composeRule.onNodeWithText("English -> ASL").assertIsDisplayed()
        composeRule.onNodeWithText("ASL -> English").assertIsDisplayed()
        composeRule.onNodeWithText("Search History").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Switch translation direction").assertIsDisplayed()
    }

    @Test
    fun translateView_clickSwitch_showsAslToEnglishUi() {
        val vm = makeVm()

        composeRule.setContent {
            MaterialTheme {
                TranslateView(
                    vm = vm,
                    hasCameraHardware = true,
                    cameraGranted = true,
                    requestCameraPermission = {}
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Switch translation direction")
            .performClick()

        composeRule.onNodeWithText("ASL -> English").assertIsDisplayed()
    }
}