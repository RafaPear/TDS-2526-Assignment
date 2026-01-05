package lobbyMenuTest.lobbyCarouselTests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import pt.isel.reversi.app.pages.game.utils.testTagBoard
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.GameCard
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.cardTestTag
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.headerBadgeTestTag
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.scorePanelTestTag
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.getCardStatus
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.storage.GameState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DrawCardTests {
    val game = Game(
        currGameName = "TestGame",
        gameState = GameState(
            players = emptyList(),
            lastPlayer = PieceType.BLACK,
            board = Board(4)
        )
    )

    @Test
    fun `verify if drawCard is displayed`() = runComposeUiTest {
        val name = game.currGameName!!
        setContent {
            GameCard(
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
        val name = game.currGameName!!
        setContent {
            GameCard(
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