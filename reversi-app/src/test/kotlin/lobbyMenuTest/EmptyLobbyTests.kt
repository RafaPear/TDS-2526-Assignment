package lobbyMenuTest

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import pt.isel.reversi.app.pages.lobby.lobbyViews.*
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EmptyLobbyTests {
    @Test
    fun `test if empty lobby view is displayed correctly`() = runComposeUiTest {
        setContent {
            Empty()
        }
        onNodeWithTag(EMPTY_LOBBY_TAG).assertExists()
    }

    @Test
    fun `verify empty lobby text is correct`() = runComposeUiTest {
        setContent {
            Empty()
        }
        onNodeWithTag(EMPTY_LOBBY_TEXT_TAG).assertExists().assertTextEquals(TEXT_EMPTY_LOBBY)
    }

    @Test
    fun `verify empty lobby icon exists`() = runComposeUiTest {
        setContent {
            Empty()
        }
        onNodeWithTag(EMPTY_LOBBY_ICON_TAG).assertExists()
    }
}