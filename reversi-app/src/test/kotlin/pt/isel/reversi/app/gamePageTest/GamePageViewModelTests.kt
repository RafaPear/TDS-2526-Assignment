package pt.isel.reversi.app.gamePageTest

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import pt.isel.reversi.app.PLACE_PIECE_SOUND
import pt.isel.reversi.app.initializeAppArgs
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.loadCoreConfig
import pt.isel.reversi.core.loadGame
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.utils.audio.AudioPool
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GamePageViewModelTests {

    fun cleanup(func: suspend () -> Unit) {
        val conf = loadCoreConfig()
        File(conf.SAVES_FOLDER).deleteRecursively()
        runBlocking { func() }
        File(conf.SAVES_FOLDER).deleteRecursively()
    }

    val game = runBlocking {
        startNewGame(
            side = 4,
            players = listOf(Player(type = PieceType.BLACK), Player(type = PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null
        )
    }

    val audioPool = initializeAppArgs(emptyArray())?.audioPool ?: AudioPool(emptyList())
    val expectedAppState = AppState(
        game = game,
        page = Page.MAIN_MENU,
        error = null,
        audioPool = audioPool
    )

    @Test
    fun `verify that the state starts correctly`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)

        assertEquals(expectedAppState.game, uut.uiState.value)
    }

    @Test
    fun `verify that set target mode works correctly`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)

        uut.setTarget(true)
        assertEquals(true, uut.uiState.value.target)

        uut.setTarget(false)
        assertEquals(false, uut.uiState.value.target)
    }

    @Test
    fun `verify that get available plays works correctly`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)

        val availablePlays = uut.getAvailablePlays()
        val expectedPlays = expectedAppState.game.getAvailablePlays()

        assertEquals(expectedPlays, availablePlays)
    }

    @Test
    fun `verify that play move works correctly`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        val coordinate = uut.getAvailablePlays().first()

        val expectedGame = appState.value.game.play(coordinate)
        testScheduler.advanceUntilIdle()

        uut.playMove(coordinate, save = false)
        testScheduler.advanceUntilIdle()

        assertEquals(expectedGame, uut.uiState.value)
    }

    @Test
    fun `verify that play move plays audio`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        val coordinate = uut.getAvailablePlays().first()

        appState.value.game.play(coordinate)
        testScheduler.advanceUntilIdle()

        uut.playMove(coordinate, save = false)

        testScheduler.advanceUntilIdle()

        appState.getStateAudioPool().run {
            getAudioTrack(PLACE_PIECE_SOUND)?.let { audioTrack ->
                assert(audioTrack.isPlaying())
            }
        }
    }

    @Test
    fun `verify that play move updates appState when save is true`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        val coordinate = uut.getAvailablePlays().first()

        val expectedGame = appState.value.game.play(coordinate)
        testScheduler.advanceUntilIdle()

        uut.playMove(coordinate, save = true)
        testScheduler.advanceUntilIdle()

        assertEquals(expectedGame, appState.value.game)
    }

    @Test
    fun `verify that play move does not update appState when save is false`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        val coordinate = uut.getAvailablePlays().first()

        uut.playMove(coordinate, save = false)
        testScheduler.advanceUntilIdle()

        assertNotEquals(appState.value.game, uut.uiState.value)
    }

    @Test
    fun `verify that play move sets error in appState when exception is thrown`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        val invalidCoordinate = Coordinate(-1, -1)

        uut.playMove(invalidCoordinate, save = false)
        testScheduler.advanceUntilIdle()

        assert(appState.value.error != null)
        assertEquals(ErrorType.CRITICAL, appState.value.error?.type)
    }

    @Test
    fun `verify that save game works correctly`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        val coordinate = uut.getAvailablePlays().first()

        uut.playMove(coordinate, save = false)
        testScheduler.advanceUntilIdle()

        assert(appState.value.game != uut.uiState.value)

        uut.save()
        testScheduler.advanceUntilIdle()

        assertEquals(appState.value.game, uut.uiState.value)
    }

    @Test
    fun `verify that start and stop polling works correctly`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)

        uut.startPolling()
        assert(uut.isPollingActive())

        uut.stopPolling()
        assert(!uut.isPollingActive())
    }

    @Test
    fun `verify that starting polling twice throws exception`() = runTest {
        val appState = mutableStateOf(expectedAppState)
        val uut = GamePageViewModel(appState, this)
        uut.startPolling()
        try {
            uut.startPolling()
            assert(false) // Should not reach here
        } catch (_: IllegalStateException) {
            assert(true) // Expected exception
        } finally {
            uut.stopPolling()
        }
    }

    @Test
    fun `verify startPolling works correctly with game refresh`() = runTest {
        cleanup {
            //Create a fake online game
            val appState = mutableStateOf(
                expectedAppState.copy(
                    game = startNewGame(
                        side = 4,
                        players = listOf(Player(type = PieceType.BLACK)),
                        firstTurn = PieceType.BLACK,
                        currGameName = "TestGame"
                    )
                )
            )
            testScheduler.advanceUntilIdle()
            loadGame(appState.value.game.currGameName!!)
            testScheduler.advanceUntilIdle()
            val oldGame = appState.value.game

            // Create the ViewModel
            val uut = GamePageViewModel(appState, this)

            // Simulate an external move by another player
            val expectedGame = oldGame.play(uut.getAvailablePlays().first())

            // Start polling and advance time to allow for refresh
            uut.startPolling()
            testScheduler.advanceUntilIdle()
            assert(uut.isPollingActive())
            uut.stopPolling()
            testScheduler.advanceUntilIdle()
            assert(!uut.isPollingActive())

            // Verify that the game state was refreshed
            assertEquals(expectedGame.gameState, uut.uiState.value.gameState)
        }
    }
}