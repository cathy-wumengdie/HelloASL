package ca.uwaterloo.helloasl.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import kotlin.test.Test

class ProfileViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileView_displays_user_name() {

        val vm = ProfileViewModel(FakeModel())

        composeTestRule.setContent {
            ProfileView(vm)
        }

        composeTestRule
            .onNodeWithText("Yanjin")
            .assertIsDisplayed()
    }

    @Test
    fun profileView_shows_learning_progress_section() {

        val vm = ProfileViewModel(FakeModel())

        composeTestRule.setContent {
            ProfileView(vm)
        }

        composeTestRule
            .onNodeWithText("Learning Progress")
            .assertIsDisplayed()
    }

    @Test
    fun profileView_shows_words_learned() {

        val vm = ProfileViewModel(FakeModel())

        composeTestRule.setContent {
            ProfileView(vm)
        }

        composeTestRule
            .onNodeWithText("Words Learned")
            .assertIsDisplayed()
    }
}