package pt.isel.reversi.game

import pt.isel.reversi.board.Board
import pt.isel.reversi.board.Coordinates
import pt.isel.reversi.board.Piece
import pt.isel.reversi.board.PieceType.WHITE
import pt.isel.reversi.board.PieceType.BLACK
import pt.isel.reversi.game.exceptions.InvalidPlay
import kotlin.test.Test
import kotlin.test.assertFailsWith


class GameLogicTests {
    @Test
    fun `getCapturablePieces should return empty list when no capturable pieces are found`() {
        var board = Board(4).addPiece(Coordinates(1, 1), BLACK)
        repeat(3) {
            board = board.addPiece(Coordinates(1, it + 2), WHITE)
        }
        val uut = GameLogic().getCapturablePieces(
            board,
            Piece(Coordinates(1, 1), BLACK),
            Coordinates(0, 1)
        )
        assert(uut.isEmpty())

        board = board.addPiece(Coordinates(2, 1), BLACK)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(2, 4), BLACK)

        val uut2 = GameLogic().getCapturablePieces(
            board,
            Piece(Coordinates(2, 1), BLACK),
            Coordinates(0, 1)
        )
        assert(uut2.isEmpty())

        board = board.addPiece(Coordinates(3, 1), BLACK)
        board = board.addPiece(Coordinates(3, 2), BLACK)
        board = board.addPiece(Coordinates(3, 3), WHITE)
        board = board.addPiece(Coordinates(3, 4), BLACK)

        val uut3 = GameLogic().getCapturablePieces(
            board,
            Piece(Coordinates(3, 1), BLACK),
            Coordinates(0, 1)
        )
        assert(uut3.isEmpty())

        val uut4 = GameLogic().getCapturablePieces(
            board,
            Piece(Coordinates(1, 1), BLACK),
            Coordinates(1, 1)
        )

        assert(uut4.isEmpty())
    }

    @Test
    /*
          1 2 3 4
        1 B W W B
        2 . W . .
        3 . . W .
        4 . . . B
     */
    fun `getCapturablePieces should return list of capturable pieces when found`() {
        val coordinate = Coordinates(1, 1)
        var board = Board(4).addPiece(coordinate, BLACK)

        board = board.addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(1, 3), WHITE)
        board = board.addPiece(Coordinates(1, 4), BLACK)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(3, 3), WHITE)
        board = board.addPiece(Coordinates(4, 4), BLACK)

        val uut = GameLogic().getCapturablePieces(
            board,
            Piece(coordinate, BLACK),
            Coordinates(1, 1)
        )
        assert(uut.size == 2)
        assert(
            uut.containsAll(
                listOf(
                    Coordinates(3, 3),
                    Coordinates(2, 2),
                )
            )
        )

        val uut2 = GameLogic().getCapturablePieces(
            board,
            Piece(coordinate, BLACK),
            Coordinates(0, 1)
        )
        assert(uut2.size == 2)
        assert(
            uut2.containsAll(
                listOf(
                    Coordinates(1, 2),
                    Coordinates(1, 3)
                )
            )
        )
    }

    @Test
    fun `getCapturablePieces when myPiece does not exist`() {
        var board = Board(4)

        board = board.addPiece(Coordinates(1, 1), WHITE)
        board = board.addPiece(Coordinates(1, 2), BLACK)

        assertFailsWith<IllegalArgumentException> {
            val uut = GameLogic().getCapturablePieces(
                board,
                Piece(Coordinates(0, 0), BLACK),
                Coordinates(0, 1)
            )
        }
    }

    @Test
    fun `getCapturablePieces when myPiece is out of bounds`() {
        val board = Board(4)
        assertFailsWith<IllegalArgumentException> {
            GameLogic().getCapturablePieces(board, Piece(Coordinates(0, 5), BLACK), Coordinates(0, 1))
        }
    }

    @Test
    fun `getCapturablePieces when my piece is at the edge and direction is out of bounds`() {
        var board = Board(4).addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(3, 3), WHITE)
        board = board.addPiece(Coordinates(4, 4), BLACK)

        val uut = GameLogic().getCapturablePieces(
            board,
            Piece(Coordinates(1, 1), BLACK),
            Coordinates(-1, -1)
        )
        assert(uut.isEmpty())
    }
    @Test
    /*
          1 2 3 4
        1 B W W B
        2 . W . .
        3 . . B W
        4 . . B B
     */
    fun `findAround should return list of coordinates around myPiece that contain findThis`() {
        val coordinate = Coordinates(3, 3)
        var board = Board(4).addPiece(coordinate, BLACK)

        board = board.addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(1, 3), WHITE)
        board = board.addPiece(Coordinates(1, 4), BLACK)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(3, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)

        val uut = GameLogic().findAround(
            board,
            Piece(coordinate, BLACK),
            WHITE
        )
        assert(uut.size == 2)
        assert(
            uut.containsAll(
                listOf(
                    Coordinates(2, 2),
                    Coordinates(3, 4),
                )
            )
        )

        val uut2 = GameLogic().findAround(
            board,
            Piece(coordinate, BLACK),
            null
        )
        assert(uut2.size == 4)
        assert(
            uut2.containsAll(
            listOf(
                Coordinates(2, 3),
                Coordinates(4, 2),
                Coordinates(2, 4),
                Coordinates(3, 2)
                )
            )
        )
    }
    @Test
    fun `findAround when myPiece is out of bounds`() {
        val board = Board(4)
        assertFailsWith<IllegalArgumentException> {
            GameLogic().findAround(board, Piece(Coordinates(0, 5), BLACK), WHITE)
        }
    }

    @Test
    /*
          1 2 3 4
        1 B W W .
        2 . W . W
        3 . . B W
        4 . . B B
     */

    fun `isValidMove should return true when there are capturable pieces` () {
        val coordinate = Coordinates(3, 3)
        var board = Board(4).addPiece(coordinate, BLACK)

        board = board.addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(1, 3), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(3, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)

        val piece = Piece(Coordinates(1, 4), BLACK)
        val uut = GameLogic().isValidMove(board,piece)

        assert(uut)
    }

    @Test
    /*
        1 2 3 4
      1 B W W .
      2 . W . W
      3 . . B W
      4 . . B B
   */
    fun `isValidMove should return false when there are no capturable pieces` () {
        val coordinate = Coordinates(3, 3)
        var board = Board(4).addPiece(coordinate, BLACK)

        board = board.addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(1, 3), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(3, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)

        val piece = Piece(Coordinates(1, 4), WHITE)
        val uut = GameLogic().isValidMove(board,piece)

        assert(!uut)
    }

    @Test
    /*
          1 2 3 4
        1 B W B .
        2 . W . W
        3 . . B W
        4 . . B B
     */
    fun `isValidMove should return false when myPiece have occupied position`() {
        val coordinate = Coordinates(1, 4)
        var board = Board(4).addPiece(Coordinates(3, 3), BLACK)

        board = board.addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(1, 3), BLACK)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(3, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)
        board = board.addPiece(Coordinates(1, 4), BLACK)

        val uut = GameLogic().isValidMove(board, Piece(coordinate, BLACK))
        assert(!uut)
        val uut2 = GameLogic().isValidMove(board, Piece(coordinate, WHITE))
        assert(!uut2)
    }

    @Test
    /*
        1 2 3 4
      1 B W W .
      2 . W . W
      3 . . B W
      4 . . B B
   */
    fun `isValidMove when myPiece is out of bounds`() {
        val board = Board(4)
        assertFailsWith<IllegalArgumentException> {
            GameLogic().isValidMove(board, Piece(Coordinates(10, 10), BLACK))
        }
    }

    @Test
    fun `isValidMove should return false when no pieces around` () {
        val board = Board(4)

        val uut = GameLogic().isValidMove(board, Piece(Coordinates(2, 2), BLACK))

        assert(!uut)
    }


    @Test
        /*
          1 2 3 4
        1 . W . .
        2 . W . W
        3 . . . .
        4 . . B B
       */
    fun `getAvailablePlays should return empty list when no moves are possible` () {
        var board = Board(4).addPiece(Coordinates(1,2), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)

        val uut = GameLogic().getAvailablePlays(board, WHITE)
        assert(uut == emptyList<Coordinates>())
        val uut2 = GameLogic().getAvailablePlays(board, BLACK)
        assert(uut2 == emptyList<Coordinates>())
    }

    @Test
            /*
              1 2 3 4
            1 B W . .
            2 . W . W
            3 . B . .
            4 . . B B
           */
    fun `getAvailablePlays should return correct coordinates for both players` () {
        var board = Board(4).addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)
        board = board.addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(3, 2), BLACK)

        val expectedMyPieceIsBlack = listOf(
            Coordinates(1, 3),
            Coordinates(3, 3)
            )
        val expectedMyPieceIsWhite = listOf(
            Coordinates(4, 2)
            )

        val uut = GameLogic().getAvailablePlays(board, BLACK)
        assert(uut == expectedMyPieceIsBlack)
        val uut2 = GameLogic().getAvailablePlays(board, WHITE)
        assert(uut2 == expectedMyPieceIsWhite)
    }

    @Test
            /*
              1 2 3 4
            1 B W . .
            2 . W . W
            3 . B . .
            4 . . B B
           */
    fun `play should throw InvalidPlay exception when move is invalid` () {
        var board = Board(4).addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)
        board = board.addPiece(Coordinates(1, 1), BLACK)
        board = board.addPiece(Coordinates(3, 2), BLACK)


        assertFailsWith<InvalidPlay> {
            val uut = GameLogic().play(board, Piece(Coordinates(3, 3), WHITE))
            val uut2 = GameLogic().play(board, Piece(Coordinates(1, 4), BLACK))
        }
    }

    @Test
            /*
             1 2 3 4
           1 . W W B
           2 . W . W
           3 . B B .
           4 . . B B
          */
    fun `play should throw InvalidPlay exception when position is already occupied` () {
        var board = Board(4).addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)
        board = board.addPiece(Coordinates(1, 3), WHITE)
        board = board.addPiece(Coordinates(3, 2), BLACK)
        board = board.addPiece(Coordinates(1, 4), BLACK)
        board = board.addPiece(Coordinates(3, 3), BLACK)

        val myPiece = Piece(Coordinates(1, 1), BLACK)

        board = board.addPiece(myPiece.coordinate,myPiece.value)

        assertFailsWith<InvalidPlay> {
            val uut = GameLogic().play(board, myPiece)
        }
    }

    @Test
            /*
              1 2 3 4
            1 . W W B
            2 . W . W
            3 . B B .
            4 . . B B
           */
    fun `play should return updated board when move is valid` () {
        var board = Board(4).addPiece(Coordinates(1, 2), WHITE)
        board = board.addPiece(Coordinates(2, 2), WHITE)
        board = board.addPiece(Coordinates(2, 4), WHITE)
        board = board.addPiece(Coordinates(4, 3), BLACK)
        board = board.addPiece(Coordinates(4, 4), BLACK)
        board = board.addPiece(Coordinates(1, 3), WHITE)
        board = board.addPiece(Coordinates(3, 2), BLACK)
        board = board.addPiece(Coordinates(1, 4), BLACK)
        board = board.addPiece(Coordinates(3, 3), BLACK)

        val myPiece = Piece(Coordinates(1, 1), BLACK)

        var expectedBoard = board.addPiece(myPiece.coordinate, myPiece.value)
        expectedBoard = expectedBoard.changePiece(Coordinates(1, 2))
        expectedBoard = expectedBoard.changePiece(Coordinates(1, 3))
        expectedBoard = expectedBoard.changePiece(Coordinates(2, 2))

        val uut = GameLogic().play(board, myPiece)

        assert(uut == expectedBoard)
    }
}