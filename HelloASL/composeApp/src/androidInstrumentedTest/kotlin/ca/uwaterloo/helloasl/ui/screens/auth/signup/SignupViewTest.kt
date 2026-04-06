package ca.uwaterloo.helloasl.ui.screens.auth.signup

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
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
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class SignupViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createDependencies(): Pair<Model, SignupViewModel> {
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
        val vm = SignupViewModel(model)
        return Pair(model, vm)
    }

    @Test
    fun signupView_rendersInitialState() {
        val (_, vm) = createDependencies()

        composeTestRule.setContent {
            SignupView(
                viewModel = vm,
                onBackToLogin = {},
                onSignupSuccess = {}
            )
        }

        composeTestRule.onAllNodesWithText("Create Account").assertCountEquals(2)
        composeTestRule.onNodeWithText("Name").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Confirm Password").assertExists()
        composeTestRule.onNodeWithText("Back to Sign In").assertExists()
    }

    @Test
    fun signupView_inputsUpdateViewModelState() {
        val (_, vm) = createDependencies()

        composeTestRule.setContent {
            SignupView(
                viewModel = vm,
                onBackToLogin = {},
                onSignupSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Name").performTextInput("John Doe")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password123")

        assertEquals("John Doe", vm.uiState.value.name)
        assertEquals("test@example.com", vm.uiState.value.email)
        assertEquals("password123", vm.uiState.value.password)
        assertEquals("password123", vm.uiState.value.confirmPassword)
    }

    @Test
    fun signupView_clickSignInNavigates() {
        val (_, vm) = createDependencies()
        var navigated = false

        composeTestRule.setContent {
            SignupView(
                viewModel = vm,
                onBackToLogin = { navigated = true },
                onSignupSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Back to Sign In").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun signupView_emptySubmitShowsError() {
        val (_, vm) = createDependencies()

        composeTestRule.setContent {
            SignupView(
                viewModel = vm,
                onBackToLogin = {},
                onSignupSuccess = {}
            )
        }

        composeTestRule.onNode(hasText("Create Account") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("Name cannot be empty").assertExists()
    }
}
