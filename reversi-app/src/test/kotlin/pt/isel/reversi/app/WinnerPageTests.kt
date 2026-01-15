package pt.isel.reversi.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.app.pages.PagesState
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.gameServices.EmptyGameService
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.core.storage.MatchPlayers
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class WinnerPageTests {

    val game = runBlocking {
        startNewGame(
            side = 4,
            players = MatchPlayers(Player(type = PieceType.BLACK), Player(type = PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
            service = EmptyGameService()
        )
    }

    val appState = AppState.empty(service = EmptyGameService()).copy(
        game = game,
        pagesState = PagesState(Page.GAME, Page.NONE)
    )

    @Test
    fun `check winner page shows correct winner`() = runComposeUiTest {

    }
}