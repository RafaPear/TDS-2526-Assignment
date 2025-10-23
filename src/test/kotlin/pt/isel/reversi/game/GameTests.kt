package pt.isel.reversi.game

import pt.isel.reversi.core.Environment.firstPlayerTurn
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.exceptions.InvalidGameException
import pt.isel.reversi.core.game.localgda.LocalGDA
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GameTests {
    @Test
    fun `play with game not started yet`() {
        val game = Game(
            dataAccess = LocalGDA(),
            players = emptyList(),
            target = false,
            playerTurn = firstPlayerTurn,
            currGameName = null,
            board = null,
        )

        assertFailsWith <InvalidGameException> {
            game.play(Coordinate(1,1))
        }
    }



}