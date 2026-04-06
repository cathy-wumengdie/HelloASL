package ca.uwaterloo.helloasl.ui.screens.auth.login

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class LoginViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createDependencies(): Pair<Model, LoginViewModel> {
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
        val vm = LoginViewModel(model)
        return Pair(model, vm)
    }

    @Test
    fun loginView_rendersInitialState() {
        val (_, vm) = createDependencies()

        composeTestRule.setContent {
            LoginView(
                viewModel = vm,
                onNavigateToSignup = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Welcome to HelloASL").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Sign In").assertExists()
        composeTestRule.onNodeWithText("Create Account").assertExists()
    }

    @Test
    fun loginView_inputsUpdateViewModelState() {
        val (_, vm) = createDependencies()

        composeTestRule.setContent {
            LoginView(
                viewModel = vm,
                onNavigateToSignup = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        assertEquals("test@example.com", vm.uiState.value.email)
        assertEquals("password123", vm.uiState.value.password)
    }

    @Test
    fun loginView_clickCreateAccountNavigates() {
        val (_, vm) = createDependencies()
        var navigated = false

        composeTestRule.setContent {
            LoginView(
                viewModel = vm,
                onNavigateToSignup = { navigated = true },
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Create Account").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun loginView_emptySubmitShowsError() {
        val (_, vm) = createDependencies()

        composeTestRule.setContent {
            LoginView(
                viewModel = vm,
                onNavigateToSignup = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Sign In").performClick()
        composeTestRule.onNodeWithText("Email and password cannot be empty").assertExists()
    }
}
