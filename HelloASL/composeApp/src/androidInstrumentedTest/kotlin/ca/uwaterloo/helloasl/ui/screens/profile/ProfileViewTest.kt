package ca.uwaterloo.helloasl.ui.screens.profile

import androidx.compose.ui.test.*
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import org.junit.Rule
import org.junit.Test

class ProfileViewTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun createVm(): ProfileViewModel {

        val db = MockDB()

        db.login("yanjin@gmail.com", "1234")

        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )

        val model = Model(repos)

        return ProfileViewModel(model)
    }

    @Test
    fun profileView_displaysSections() {

        val vm = createVm()

        rule.setContent {
            ProfileView(vm)
        }

        rule.onNodeWithText("Learning Progress")
            .assertExists()

        rule.onNodeWithText("Words Learned")
            .assertExists()

        rule.onNodeWithText("Starred Signs")
            .assertExists()
    }

    @Test
    fun setGoalsButton_opensDialog() {

        val vm = createVm()

        rule.setContent {
            ProfileView(vm)
        }

        rule.onNodeWithText("Set Learning Goals")
            .performClick()

        rule.onNodeWithText("Set Learning Goals")
            .assertExists()
    }

    @Test
    fun accountButton_exists() {

        val vm = createVm()

        rule.setContent {
            ProfileView(vm)
        }

        rule.onNodeWithText("Account")
            .assertExists()
    }

    @Test
    fun licenseButton_exists() {

        val vm = createVm()

        rule.setContent {
            ProfileView(vm)
        }

        rule.onNodeWithText("License")
            .assertExists()
    }

    @Test
    fun signOutButton_exists() {

        val vm = createVm()

        rule.setContent {
            ProfileView(vm)
        }

        rule.onNodeWithText("Sign out")
            .assertExists()
    }
}