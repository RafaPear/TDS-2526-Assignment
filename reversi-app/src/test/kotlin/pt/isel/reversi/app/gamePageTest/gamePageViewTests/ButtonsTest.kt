package pt.isel.reversi.app.gamePageTest.gamePageViewTests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import pt.isel.reversi.app.pages.game.utils.TargetButton
import pt.isel.reversi.app.pages.game.utils.testTagTargetButtons
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ButtonsTest {
    @Test
    fun `check if the Target button is displayed and clickable`() = runComposeUiTest {
        var clicked = false
        val target = true
        setContent {
            TargetButton(
                target = target,
                freeze = false,
                onClick = { clicked = true }
            )
        }

        onNodeWithTag(testTag = testTagTargetButtons(target = target))
            .assertExists()
            .performClick()

        assert(value = clicked) { "Button was not clicked" }
    }

    @Test
    fun `check if the Target button is disabled when freeze is true`() = runComposeUiTest {
        var clicked = false
        val target = false
        setContent {
            TargetButton(
                target = target,
                freeze = true,
                onClick = { clicked = true }
            )
        }

        onNodeWithTag(testTag = testTagTargetButtons(target = target))
            .assertExists()
            .performClick()

        assert(value = !clicked) { "Button should be disabled and not clickable" }
    }
}
