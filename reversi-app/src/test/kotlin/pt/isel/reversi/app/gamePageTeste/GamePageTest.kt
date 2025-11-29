package pt.isel.reversi.app.gamePageTeste

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.gamePage.GamePage
import pt.isel.reversi.app.gamePage.testTagCellView
import pt.isel.reversi.app.gamePage.testTagGamePage
import pt.isel.reversi.app.gamePage.testTagPlayerScore
import pt.isel.reversi.app.gamePage.testTagTitle
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.startNewGame
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class GamePageTest {
    val game = runBlocking {
        startNewGame(
            side = 4,
            players = listOf(Player(type = PieceType.BLACK), Player(type = PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null
        )
    }

    @Test
    fun `check if the title is displayed correctly`() = runComposeUiTest {
        val name = "test"
        val expectedAppState = AppState(
            game = game.copy(currGameName = name),
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState)
        }
        onNodeWithTag(testTag = testTagTitle(gameName = name))
            .onChild().assertExists()
    }

    @Test
    fun `check if the title is not displayed when game name is null`() = runComposeUiTest {
        val name = null
        val expectedAppState = AppState(
            game = game.copy(currGameName = name),
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState)
        }
        onNodeWithTag(testTag = testTagTitle(gameName = name))
            .onChild().assertDoesNotExist()
    }

    @Test
    fun `check if the GamePage is displayed`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState)
        }
        onNodeWithTag(testTag = testTagGamePage())
            .assertExists()
    }

    @Test
    fun `check if have a board`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState)
        }
        onNodeWithTag(testTag = testTagGamePage()).assertExists()
    }

    @Test
    fun `check if have a two players on Score`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState)
        }

        val players = game.gameState?.players

        players?.forEach { player ->
            onNodeWithTag(testTag = testTagPlayerScore(player)).assertExists()
        }
    }

    @Test
    fun `check if don't have players on Score when gameState is null`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game.copy(gameState = null),
            page = Page.GAME,
            error = null
        )

        setContent {
            GamePage(mutableStateOf(value = expectedAppState))
        }

        onNodeWithTag(testTag = testTagPlayerScore(game.gameState?.players[0]!!))
            .assertDoesNotExist()
    }

    @Test
    fun `check if player score change after a move`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState)
        }

        val players = game.gameState?.players!!

        players.forEach { player ->
            onNodeWithTag(testTag = testTagPlayerScore(player)).assertExists()
        }

        val validMove = game.getAvailablePlays().first()

        val expectedGameState = game.play(coordinate = validMove)

        //find the valid cell and perform click
        onNodeWithTag(testTag = testTagCellView(coordinate = validMove)).performClick()

        val newPlayers = expectedGameState.gameState?.players!!

        //verify if the players score have changed
        repeat(times = 2) {
            onNodeWithTag(testTag = testTagPlayerScore(players[it])).assertDoesNotExist()
            onNodeWithTag(testTag = testTagPlayerScore(player = newPlayers[it])).assertExists()
        }
    }

    @Test
    fun `check if player score not change if freeze is true`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            GamePage(appState, freeze = true)
        }

        val players = game.gameState?.players!!

        players.forEach { player ->
            onNodeWithTag(testTag = testTagPlayerScore(player)).assertExists()
        }

        val validMove = game.getAvailablePlays().first()

        //find the valid cell and perform click
        onNodeWithTag(testTag = testTagCellView(coordinate = validMove)).performClick()

        //verify if the players score have not changed
        players.forEach { player ->
            onNodeWithTag(testTag = testTagPlayerScore(player)).assertExists()
        }
    }
}