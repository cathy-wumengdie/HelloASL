package ca.uwaterloo.helloasl.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignVideoPlayerTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsLoadingThenUnavailableForMissingResource() {
        composeRule.setContent {
            SignVideoPlayer(resourcePath = "files/video/missing.mp4", modifier = Modifier)
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Loading video…").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Video unavailable").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Video unavailable").assertExists()
    }

    @Test
    fun playButtonTogglesState() {
        composeRule.setContent {
            SignVideoPlayer(resourcePath = "files/video/hello.mp4", modifier = Modifier)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Loading video…").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNode(hasContentDescription("Play"))
            .assertExists()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Pause", substring = false).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Pause").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithContentDescription("Pause").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasContentDescription("Pause")).assertExists()
    }
}
