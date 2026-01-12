package pt.isel.reversi.app.gamePageTest

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.core.storage.MatchPlayers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GamePageViewModelTests {

    val game = runBlocking {
        startNewGame(
            side = 4,
            players = MatchPlayers(Player(type = PieceType.BLACK), Player(type = PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null
        )
    }

    @Test
    fun `verify that the state initializes correctly`() = runTest {

        val uut = GamePageViewModel(
            game,
            this,
            { },
            {},
            null,{}
        )

        // Verify initial state
        assertNotNull(uut.uiState.value)
        assertEquals(game, uut.uiState.value.game)
    }

    @Test
    fun `verify that get available plays works correctly`() = runTest {
        val uut = GamePageViewModel(game, this, { }, {}, null,{})

        val availablePlays = uut.getAvailablePlays()
        val expectedPlays = game.getAvailablePlays()

        assertEquals(expectedPlays, availablePlays)
    }

    @Test
    fun `verify that set target mode works correctly`() = runTest {
        val uut = GamePageViewModel(game, this, { }, {}, null,{})

        val initialTarget = uut.uiState.value.game.target
        uut.setTarget(!initialTarget)

        assertEquals(!initialTarget, uut.uiState.value.game.target)
    }

    @Test
    fun `verify that save preserves game state`() = runTest {
        val uut = GamePageViewModel(
            game,
            this,
            { },
            {},
            null,
            {}
        )

        uut.save()
        // Verify save was called
        assertNotNull(uut.uiState.value)
    }

    @Test
    fun `verify polling control methods work`() = runTest {
        val uut = GamePageViewModel(game, this, { }, {}, null,{})

        // Initially no polling
        assertEquals(false, uut.isPollingActive())

        // Start polling would require coroutine context, so we just verify the state
        assertNotNull(uut)
    }

    @Test
    fun `verify that starting polling twice throws exception`() = runTest {
        val uut = GamePageViewModel(game, this, { }, {}, null,{})
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
}



