package pt.isel.reversi.app

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.app.state.AppState
import pt.isel.reversi.app.app.state.GameSession
import pt.isel.reversi.app.app.state.PagesState
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.app.pages.PagesState
import pt.isel.reversi.app.pages.game.GamePage
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.ReversiScope
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.gameServices.EmptyGameService
import pt.isel.reversi.core.game.startNewGame
import pt.isel.reversi.core.gameState.MatchPlayers
import pt.isel.reversi.core.gameState.Player
import pt.isel.reversi.utils.BASE_FOLDER
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class WinnerPageTests {

    @BeforeTest
    @AfterTest
    fun cleanup() {
        File(BASE_FOLDER).deleteRecursively()
    }

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
        gameSession = GameSession(game, null),
        pagesState = PagesState(Page.GAME, Page.NONE, null)
    )

    @Test
    fun `check winner page shows correct winner`() = runComposeUiTest {
        appState
        assert(false)
    }
}