package pt.isel.reversi.game

import pt.isel.reversi.core.board.Coordinate
import kotlin.test.Test
import pt.isel.reversi.core.game.localgda.LocalGDA
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.exceptions.InvalidPlayException
import pt.isel.reversi.core.game.firstPlayerTurn

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

        assertFailsWith <InvalidPlayException> {
            game.play(Coordinate(1,1))
        }
    }



}