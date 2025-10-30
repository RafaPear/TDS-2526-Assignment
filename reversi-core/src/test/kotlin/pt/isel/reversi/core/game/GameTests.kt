package pt.isel.reversi.core.game

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.exceptions.InvalidGameException
import pt.isel.reversi.core.game.exceptions.InvalidPlayException
import pt.isel.reversi.core.game.localgda.LocalGDA
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameTests {
    @Test
    fun `play with game not started yet`() {
        val game = Game()

        assertFailsWith<InvalidGameException> {
            game.play(Coordinate(1, 1))
        }
    }

    /*
          1 2 3 4
        1 . . . .
        2 . W B .
        3 . B W .
        4 . . . .
     */
    @Test
    fun `play with 1 player in local game and not his turn fails`() {
        val uut = Game().startNewGame(
            side = 4,
            players = listOf(Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertFailsWith<InvalidPlayException> {
            uut.play(Coordinate(1, 2))
        }
    }

    /*
          1 2 3 4
        1 . . . .
        2 . W B .
        3 . B W .
        4 . . . .
     */
    @Test
    fun `play with 1 player in local game and is your turn succeeds`() {
        val expectedBoard = Board(4).startPieces()
            .addPiece(Coordinate(1, 2), PieceType.BLACK)
            .changePiece(Coordinate(2, 2))

        val uut = Game().startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assert(expectedBoard == uut.play(Coordinate(1, 2)).board)
    }

    @Test
            /*
          1 2 3 4
        1 . . . .
        2 . W B .
        3 . B W .
        4 . . . .
            */
    fun `play in local game with 2 players succeeds`() {
        val expectedBoard = Board(4).startPieces()
            .addPiece(Coordinate(1, 2), PieceType.BLACK)
            .changePiece(Coordinate(2, 2))

        val expectedGame = Game(
            dataAccess = LocalGDA(),
            board = expectedBoard,
            target = false,
            players = listOf(
                Player(PieceType.BLACK, 4),
                Player(PieceType.WHITE, 1)
            ),
            playerTurn = PieceType.WHITE,
            currGameName = null,
        )

        val uut = Game().startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        ).play(Coordinate(1, 2))

        assert(expectedGame.equals(uut))
    }

    @Test
            /*
          1 2 3 4
        1 . . . .
        2 . W W W
        3 . B B B
        4 . . . .
               */
    fun `play in local game verify players point update correctly and playerTurn after normal plays`() {
        val expectedBlackPoints = 3
        val expectedWhitePoints = 3
        val expectedPlayerTurn = PieceType.BLACK

        val uut = Game().startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )
            .play(Coordinate(3, 4))
            .play(Coordinate(2, 4))

        assertEquals(expectedBlackPoints, uut.players.find { it.type == PieceType.BLACK }!!.points)
        assertEquals(expectedWhitePoints, uut.players.find { it.type == PieceType.WHITE }!!.points)
        assertEquals(expectedPlayerTurn, uut.playerTurn)
    }

    @Test
    fun `play with players empty fails`() {
        val uut = Game().startNewGame(
            side = 4,
            players = emptyList(),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertFailsWith<InvalidGameException> {
            uut.play(Coordinate(1, 2))
        }
    }

    @Test
    fun `pieceOptions in local game with 1 player return inverted pieces`() {
        val expectedType = listOf(PieceType.BLACK)
        val uut = Game().startNewGame(
            side = 4,
            players = listOf(Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertEquals(expectedType, uut.pieceOptions())
    }

    @Test
    fun `pieceOptions in local game with 2 players return emptyList`() {
        val expectedType = emptyList<PieceType>()
        val uut = Game().startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertEquals(expectedType, uut.pieceOptions())
    }

    @Test
    fun `pieceOptions in local game with no players return all piece types`() {
        val expectedType = listOf(PieceType.BLACK, PieceType.WHITE)
        val uut = Game().startNewGame(
            side = 4,
            players = emptyList(),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertEquals(expectedType, uut.pieceOptions())
    }
}