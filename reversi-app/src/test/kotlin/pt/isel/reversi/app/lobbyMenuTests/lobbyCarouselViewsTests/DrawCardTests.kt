package lobbyMenuTest.lobbyCarouselTests.lobbyCarouselViewsTests

import androidx.compose.ui.test.*
import pt.isel.reversi.app.pages.game.testTagBoard
import pt.isel.reversi.app.pages.game.testTagCellView
import pt.isel.reversi.app.pages.lobby.LobbyLoadedState
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.*
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.getCardStatus
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.ReversiScope
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.MatchPlayers
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DrawCardTests {
    val game = Game(
        currGameName = "TestGame",
        gameState = GameState(
            players = MatchPlayers(Player(PieceType.WHITE)),
            lastPlayer = PieceType.BLACK,
            board = Board(4).startPieces(),
        ),
        myPiece = PieceType.WHITE,
    )

    val reversiScope = ReversiScope(AppState.empty())

    @Test
    fun `verify if drawCard is displayed`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)

        setContent {
            reversiScope.GameCard(
                game = LobbyLoadedState(game.gameState!!, name),
                enabled = false,
                cardData = getCardStatus(lobbyState, name),
                onClick = {}
            )
        }

        onNodeWithTag(cardTestTag(name)).assertExists()
    }

    @Test
    fun `verify if drawCard is displayed correctly`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)

        setContent {
            reversiScope.GameCard(
                game = lobbyState,
                enabled = false,
                cardData = getCardStatus(lobbyState, name),
                onClick = {}
            )
        }

        onNodeWithTag(cardTestTag(name)).assertExists()
        onNodeWithTag(headerBadgeTestTag(name), true).assertExists()
        onNodeWithTag(testTagBoard(), true).assertExists()
        onNodeWithTag(scorePanelTestTag(name), true).assertExists()
    }

    @Test
    fun `verify if Card is enabled`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)

        setContent {
            reversiScope.GameCard(
                game = lobbyState,
                enabled = true,
                cardData = getCardStatus(lobbyState, name),
                onClick = {}
            )
        }

        onNodeWithTag(cardTestTag(name)).assertIsEnabled()
    }

    @Test
    fun `verify if Card is disabled`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)

        setContent {
            reversiScope.GameCard(
                game = LobbyLoadedState(game.gameState!!, name),
                enabled = false,
                cardData = getCardStatus(lobbyState, name),
                onClick = {}
            )
        }

        onNodeWithTag(cardTestTag(name)).assertIsNotEnabled()
    }

    @Test
    fun `veify id header badge displays correct game name and status`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)
        val cardStatus = getCardStatus(lobbyState, name)

        setContent {
            reversiScope.GameCard(
                game = lobbyState,
                enabled = false,
                cardData = cardStatus,
                onClick = {}
            )
        }

        onNodeWithTag(statusTextTestTag(name), true).assertTextContains(name)
        onNodeWithTag(statusBadgeTestTag(name), true).assertTextContains(cardStatus.text)
    }

    @Test
    fun `verify if board is not clickable`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)

        setContent {
            reversiScope.GameCard(
                game = lobbyState,
                enabled = false,
                cardData = getCardStatus(lobbyState, name),
                onClick = {}
            )
        }
        println(game.getAvailablePlays())
        val valideCoords = game.getAvailablePlays().first()
        onNodeWithTag(testTagBoard(), true).assertHasNoClickAction()
        onNodeWithTag(testTagCellView(valideCoords), true).assertHasNoClickAction()
    }

    @Test
    fun `veify if score panel displays correct scores and pieces`() = runComposeUiTest {
        val name = game.currGameName!!
        val lobbyState = LobbyLoadedState(game.gameState!!, name)

        setContent {
            reversiScope.GameCard(
                game = lobbyState,
                enabled = false,
                cardData = getCardStatus(lobbyState, name),
                onClick = {}
            )
        }

        val board = game.gameState!!.board
        val totalBlack = board.totalBlackPieces
        val totalWhite = board.totalWhitePieces

        val blackPieceTestTag = scoreItemPieceTestTag(
            scorePainelTestTag = scorePanelTestTag(gameId = name),
            pieceType = PieceType.BLACK
        )

        val whitePieceTestTag = scoreItemPieceTestTag(
            scorePainelTestTag = scorePanelTestTag(gameId = name),
            pieceType = PieceType.WHITE
        )

        val blackPiecesScoreTestTag = scoreItemScoreTestTag(
            scorePainelTestTag = scorePanelTestTag(gameId = name),
            pieceType = PieceType.BLACK,
            score = totalBlack
        )

        val whitePiecesScoreTestTag = scoreItemScoreTestTag(
            scorePainelTestTag = scorePanelTestTag(gameId = name),
            pieceType = PieceType.WHITE,
            score = totalWhite
        )

        onNodeWithTag(blackPieceTestTag, true).assertExists()
        onNodeWithTag(whitePieceTestTag, true).assertExists()
        onNodeWithTag(blackPiecesScoreTestTag, true).assertTextContains(totalBlack.toString())
        onNodeWithTag(whitePiecesScoreTestTag, true).assertTextContains(totalWhite.toString())
    }
}