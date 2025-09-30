import pt.isel.reversi.Board
import kotlin.test.Test
import kotlin.test.assertFailsWith

class BoardTests {

    // Board tests

    @Test
    fun `Create Board with side outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(-1)
            Board(0)
            Board(2)
            Board(33)
        }
    }

    @Test
    fun `Create Board with odd side within range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(7)
            Board(25)
            Board(15)
        }
    }

    @Test
    fun `Create Board with startPieces succeeds`() {
        var uut = Board(6).startPieces()
        assert(uut[3, 'c'] == Board.PieceType.WHITE)
        assert(uut[3, 'd'] == Board.PieceType.BLACK)
        assert(uut[4, 'c'] == Board.PieceType.BLACK)
        assert(uut[4, 'd'] == Board.PieceType.WHITE)
        uut = Board(26).startPieces()
        assert(uut[13, 'm'] == Board.PieceType.WHITE)
        assert(uut[13, 'n'] == Board.PieceType.BLACK)
        assert(uut[14, 'm'] == Board.PieceType.BLACK)
        assert(uut[14, 'n'] == Board.PieceType.WHITE)
        uut = Board(16).startPieces()
        assert(uut[8, 'h'] == Board.PieceType.WHITE)
        assert(uut[8, 'i'] == Board.PieceType.BLACK)
        assert(uut[9, 'h'] == Board.PieceType.BLACK)
        assert(uut[9, 'i'] == Board.PieceType.WHITE)
    }


    @Test
    fun `get function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8)[-1, 1]
            Board(8)[0, 1]
            Board(8)[27, 1]
        }
    }

    @Test
    fun `get function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8)[1, -1]
            Board(8)[1, 0]
            Board(8)[1, 27]
            Board(8)[1, '@']
            Board(8)[1, '[']
        }
    }

    @Test
    fun `get function with index outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8)[-1]
            Board(8)[64]
        }
    }

    @Test
    fun `get function with no piece at position `() {
        assert(Board(4)[1, 'a'] == null)
        assert(Board(4)[4, 4] == null)
        assert(Board(4)[5] == null)
    }
    @Test
    fun `get and addPiece function with valid row and col (Char) succeeds`() {
        var uut = Board(4)
        uut = uut.addPiece(1, 'a', Board.PieceType.WHITE)
        assert(uut[1, 'a'] == Board.PieceType.WHITE)
    }

    @Test
    fun `get and addPiece function with valid row and col (Int) succeeds`() {
        val side = 4
        var uut = Board(side)
        uut = uut.addPiece(4, 2, Board.PieceType.BLACK)
        assert(uut[4, 2] == Board.PieceType.BLACK)
    }

    @Test
    fun `get and addPiece function with valid index succeeds`() {
        val side = 4
        var uut = Board(side)
        uut = uut.addPiece(5, Board.PieceType.BLACK)
        assert(uut[5] == Board.PieceType.BLACK)
    }

    @Test
    fun `changePiece function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).changePiece(row = -1, col = 1)
            Board(8).changePiece(row = 0, col = 1)
            Board(8).changePiece(row = 27, col = 1)
        }
    }

    @Test
    fun `changePiece function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).changePiece(row = 1, col = -1)
            Board(8).changePiece(row = 1, col = 0)
            Board(8).changePiece(row = 1, col = 27)
            Board(8).changePiece(row = 1, col = '@')
            Board(8).changePiece(row = 1, col = '[')
        }
    }
    @Test
    fun `changePiece function with no piece at position does nothing`() {
        var uut = Board(4)
        assertFailsWith<IllegalArgumentException> {
            uut = uut.changePiece(1, 'a')
        }
    }

    @Test
    fun `changePiece function with indenx outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).changePiece(-1)
            Board(8).changePiece(64)
        }
    }
    @Test
    fun `changePiece function with valid row and col (Char) succeeds`() {
        var uut = Board(4).addPiece(1, 'a', Board.PieceType.WHITE)
        uut = uut.changePiece(1, 'a')
        assert(uut[1, 'a'] == Board.PieceType.BLACK)
        uut = uut.addPiece(3, 'd', Board.PieceType.BLACK)
        uut = uut.changePiece(3, 'd')
        assert(uut[3, 'd'] == Board.PieceType.WHITE)

    }

    @Test
    fun `changePiece function with valid row and col (Int) succeeds`() {
        var uut = Board(4).addPiece(4, 2, Board.PieceType.BLACK)
        uut = uut.changePiece(4, 2)
        assert(uut[4, 2] == Board.PieceType.WHITE)
        uut = uut.addPiece(2, 3, Board.PieceType.WHITE)
        uut = uut.changePiece(2, 3)
        assert(uut[2, 3] == Board.PieceType.BLACK)
    }

    @Test
    fun `addPiece function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException>{
            Board(8).addPiece(row = -1, col = 1, value = Board.PieceType.BLACK)
            Board(8).addPiece(row = 0, col = 1, value = Board.PieceType.BLACK)
            Board(8).addPiece(row = 27, col = 1, value = Board.PieceType.BLACK)
        }
    }

    @Test
    fun `addPiece function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException>{
            Board(8).addPiece(row = 1, col = -1, value = Board.PieceType.BLACK)
            Board(8).addPiece(row = 1, col = 0, value = Board.PieceType.BLACK)
            Board(8).addPiece(row = 1, col = 27, value = Board.PieceType.BLACK)
            Board(8).addPiece(row = 1, col = '@', value = Board.PieceType.BLACK)
            Board(8).addPiece(row = 1, col = '[', value = Board.PieceType.BLACK)
        }
    }

    @Test
    fun `addPiece function with index outside range fails`() {
        assertFailsWith<IllegalArgumentException>{
            Board(8).addPiece(-1, Board.PieceType.BLACK)
            Board(8).addPiece(64, Board.PieceType.BLACK)
        }
    }

    @Test
    fun `addPiece function with piece already at position fails`() {
        assertFailsWith <IllegalArgumentException>{
            var uut = Board(4).addPiece(1, 'a', Board.PieceType.WHITE)
            uut = uut.addPiece(1, 'a', Board.PieceType.BLACK)
            var uut2 = Board(4).addPiece(5, Board.PieceType.WHITE)
            uut2 = uut.addPiece(5, Board.PieceType.BLACK)
            var uut3 = Board(4).addPiece(4, 2, Board.PieceType.WHITE)
            uut3 = uut.addPiece(4, 2, Board.PieceType.BLACK)
        }
    }
}