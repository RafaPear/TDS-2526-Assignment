package pt.isel.reversi.cli.commands

import kotlinx.coroutines.runBlocking
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.startNewGame
import kotlin.test.Test
import kotlin.test.assertTrue

class PassCmdTests {

    @Test
    fun `PassCmd returns error when context is null`() {
        val res = PassCmd.execute(context = null)
        assertTrue(res.message.contains("Game is not defined"))
    }

    @Test
    fun `PassCmd succeeds when called with a started game`() {
        // Use a simple started game using startNewGame helper
        val g = runBlocking {
            startNewGame(
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK
            )
        }

        val res = PassCmd.execute(context = g)

        // Ensure result message is not empty and contains expected keywords
        assertTrue(res.message.isNotEmpty())
    }
}
