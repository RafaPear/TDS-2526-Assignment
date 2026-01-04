package pt.isel.reversi.app.gamePageTest

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.*
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.pages.game.GamePage
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.game.utils.testTagBoard
import pt.isel.reversi.app.pages.game.utils.testTagCellView
import pt.isel.reversi.app.pages.game.utils.testTagPlayerScore
import pt.isel.reversi.app.pages.game.utils.testTagTargetButtons
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.utils.audio.AudioPool
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class GamePageTests {

    val game = runBlocking {
        startNewGame(
            side = 4,
            players = listOf(Player(type = PieceType.BLACK), Player(type = PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null
        )
    }

    @Test
    fun `check if player score change after a move`() = runComposeUiTest {

        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null,
            audioPool = AudioPool(emptyList())
        )

        val appState = mutableStateOf(value = expectedAppState)
        setContent {
            val scope = rememberCoroutineScope()
            val gameViewModel = GamePageViewModel(appState, scope)

            GamePage(gameViewModel)
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
            error = null,
            audioPool = AudioPool(emptyList())
        )

        val appState = mutableStateOf(value = expectedAppState)

        setContent {
            val scope = rememberCoroutineScope()
            val gameViewModel = GamePageViewModel(appState, scope)

            GamePage(gameViewModel, freeze = true)
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


    @Test
    fun `check if ghost pieces shows when target mode is on`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null,
            audioPool = AudioPool(emptyList())
        )

        val appState = mutableStateOf(value = expectedAppState)

        val board = game.gameState!!.board
        val expectedGhostPieces = game.getAvailablePlays().size
        val expectedPieces = board.totalBlackPieces + board.totalWhitePieces

        setContent {
            val scope = rememberCoroutineScope()
            val gameViewModel = GamePageViewModel(appState, scope)

            GamePage(gameViewModel)
        }

        var countPieces = 0

        //check if the pieces are correct before target mode
        onNodeWithTag(testTagBoard(), useUnmergedTree = true)
            .onChildren().fetchSemanticsNodes().forEach { cellNode ->
                val cell = cellNode.children
                if (cell.isNotEmpty()) countPieces++
            }

        assertEquals(expectedPieces, countPieces)

        //click on target mode button
        onNodeWithTag(testTagTargetButtons(false)).performClick()

        //check if the pieces are correct after target mode
        countPieces = 0
        onNodeWithTag(testTagBoard(), useUnmergedTree = true)
            .onChildren().fetchSemanticsNodes().forEach { cellNode ->
                val cell = cellNode.children
                if (cell.isNotEmpty()) countPieces++
            }

        assertEquals(expectedPieces + expectedGhostPieces, countPieces)
    }

    @Test
    fun `check if ghost pieces not shows when target mode is on and freeze is true`() = runComposeUiTest {
        val expectedAppState = AppState(
            game = game,
            page = Page.GAME,
            error = null,
            audioPool = AudioPool(emptyList())
        )

        val appState = mutableStateOf(value = expectedAppState)

        val board = game.gameState!!.board
        val expectedPieces = board.totalBlackPieces + board.totalWhitePieces

        setContent {
            val scope = rememberCoroutineScope()
            val gameViewModel = GamePageViewModel(appState, scope)

            GamePage(gameViewModel, freeze = true)
        }

        var countPieces = 0

        //check if the pieces are correct before target mode
        onNodeWithTag(testTagBoard(), useUnmergedTree = true)
            .onChildren().fetchSemanticsNodes().forEach { cellNode ->
                val cell = cellNode.children
                if (cell.isNotEmpty()) countPieces++
            }

        assertEquals(expectedPieces, countPieces)

        //click on target mode button
        onNodeWithTag(testTagTargetButtons(false)).performClick()

        //check if the pieces not changed after target mode
        countPieces = 0
        onNodeWithTag(testTagBoard(), useUnmergedTree = true)
            .onChildren().fetchSemanticsNodes().forEach { cellNode ->
                val cell = cellNode.children
                if (cell.isNotEmpty()) countPieces++
            }

        assertEquals(expectedPieces, countPieces)
    }
}