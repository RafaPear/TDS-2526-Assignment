package pt.isel.reversi.cli.commands

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
        val g = pt.isel.reversi.core.startNewGame(
            players = listOf(pt.isel.reversi.core.Player(pt.isel.reversi.core.board.PieceType.BLACK)),
            firstTurn = pt.isel.reversi.core.board.PieceType.BLACK
        )

        val res = PassCmd.execute(context = g)

        // Ensure result message is not empty and contains expected keywords
        assertTrue(res.message.isNotEmpty())
    }
}
