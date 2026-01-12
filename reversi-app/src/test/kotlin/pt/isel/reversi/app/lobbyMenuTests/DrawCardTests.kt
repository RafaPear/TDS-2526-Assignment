package pt.isel.reversi.app.lobbyMenuTests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.pages.game.testTagBoard
import pt.isel.reversi.app.pages.lobby.LobbyLoadedState
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.GameCard
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.cardTestTag
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.headerBadgeTestTag
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.scorePanelTestTag
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.getCardStatus
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.MatchPlayers
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DrawCardTests {
    val game = LobbyLoadedState(
        name = "TestGame",
        gameState = GameState(
            players = MatchPlayers(),
            lastPlayer = PieceType.BLACK,
            board = Board(4)
        )
    )

    val reversiScope = ReversiScope(AppState.empty())

    @Test
    fun `verify if drawCard is displayed`() = runComposeUiTest {
        val name = game.name
        setContent {
            reversiScope.GameCard(
                game = game,
                enabled = false,
                cardData = getCardStatus(game, name),
                onClick = {}
            )
        }

        onNodeWithTag(cardTestTag(name)).assertExists()
    }

    @Test
    fun `verify if drawCard is displayed correctly`() = runComposeUiTest {
        val name = game.name
        setContent {
            reversiScope.GameCard(
                game = game,
                enabled = false,
                cardData = getCardStatus(game, name),
                onClick = {}
            )
        }

        onNodeWithTag(cardTestTag(name)).assertExists()
        onNodeWithTag(headerBadgeTestTag(name), true).assertExists()
        onNodeWithTag(testTagBoard(), true).assertExists()
        onNodeWithTag(scorePanelTestTag(name), true).assertExists()
    }
}