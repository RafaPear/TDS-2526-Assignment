import pt.isel.reversi.board.Board
import pt.isel.reversi.board.Coordinates
import pt.isel.reversi.board.PieceType
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
        assert(uut[Coordinates(3, 'c')] == PieceType.WHITE)
        assert(uut[Coordinates(3, 'd')] == PieceType.BLACK)
        assert(uut[Coordinates(4, 'c')] == PieceType.BLACK)
        assert(uut[Coordinates(4, 'd')] == PieceType.WHITE)
        uut = Board(26).startPieces()
        assert(uut[Coordinates(13, 'm')] == PieceType.WHITE)
        assert(uut[Coordinates(13, 'n')] == PieceType.BLACK)
        assert(uut[Coordinates(14, 'm')] == PieceType.BLACK)
        assert(uut[Coordinates(14, 'n')] == PieceType.WHITE)
        uut = Board(16).startPieces()
        assert(uut[Coordinates(8, 'h')] == PieceType.WHITE)
        assert(uut[Coordinates(8, 'i')] == PieceType.BLACK)
        assert(uut[Coordinates(9, 'h')] == PieceType.BLACK)
        assert(uut[Coordinates(9, 'i')] == PieceType.WHITE)
    }


    @Test
    fun `get function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8)[Coordinates(-1, 1)]
            Board(8)[Coordinates(0, 1)]
            Board(8)[Coordinates(27, 1)]
        }
    }

    @Test
    fun `get function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8)[Coordinates(1, -1)]
            Board(8)[Coordinates(1, 0)]
            Board(8)[Coordinates(1, 27)]
            Board(8)[Coordinates(1, '@')]
            Board(8)[Coordinates(1, '[')]
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
        assert(Board(4)[Coordinates(1, 'a')] == null)
        assert(Board(4)[Coordinates(4, 4)] == null)
        assert(Board(4)[5] == null)
    }
    @Test
    fun `get and addPiece function with valid row and col (Char) succeeds`() {
        var uut = Board(4)
        uut = uut.addPiece(Coordinates(1, 'a'), PieceType.WHITE)
        assert(uut[Coordinates(1, 'a')] == PieceType.WHITE)
    }

    @Test
    fun `get and addPiece function with valid row and col (Int) succeeds`() {
        val side = 4
        var uut = Board(side)
        uut = uut.addPiece(Coordinates(4, 2), PieceType.BLACK)
        assert(uut[Coordinates(4, 2)] == PieceType.BLACK)
    }

    @Test
    fun `get and addPiece function with valid index succeeds`() {
        val side = 4
        var uut = Board(side)
        uut = uut.addPiece(5, PieceType.BLACK)
        assert(uut[5] == PieceType.BLACK)
    }

    @Test
    fun `changePiece function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).changePiece(Coordinates(-1,1))
            Board(8).changePiece(Coordinates(0,1))
            Board(8).changePiece(Coordinates(27,1))
        }
    }

    @Test
    fun `changePiece function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).changePiece(Coordinates(1,-1))
            Board(8).changePiece(Coordinates(1,0))
            Board(8).changePiece(Coordinates(1,27))
            Board(8).changePiece(Coordinates(1,'@'))
            Board(8).changePiece(Coordinates(1,'['))
        }
    }
    @Test
    fun `changePiece function with no piece at position does nothing`() {
        var uut = Board(4)
        assertFailsWith<IllegalArgumentException> {
            uut = uut.changePiece(Coordinates(1, 'a'))
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
        var uut = Board(4).addPiece(Coordinates(1, 'a'), PieceType.WHITE)
        uut = uut.changePiece(Coordinates(1, 'a'))
        assert(uut[Coordinates(1, 'a')] == PieceType.BLACK)
        uut = uut.addPiece(Coordinates(3, 'd'), PieceType.BLACK)
        uut = uut.changePiece(Coordinates(3, 'd'))
        assert(uut[Coordinates(3, 'd')] == PieceType.WHITE)

    }

    @Test
    fun `changePiece function with valid row and col (Int) succeeds`() {
        var uut = Board(4).addPiece(Coordinates(4, 2), PieceType.BLACK)
        uut = uut.changePiece(Coordinates(4, 2))
        assert(uut[Coordinates(4, 2)] == PieceType.WHITE)
        uut = uut.addPiece(Coordinates(2, 3), PieceType.WHITE)
        uut = uut.changePiece(Coordinates(2, 3))
        assert(uut[Coordinates(2, 3)] == PieceType.BLACK)
    }

    @Test
    fun `addPiece function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException>{
            Board(8).addPiece(Coordinates(-1, 1), value = PieceType.BLACK)
            Board(8).addPiece(Coordinates(0,1), value = PieceType.BLACK)
            Board(8).addPiece(Coordinates(27, 1), value = PieceType.BLACK)
        }
    }

    @Test
    fun `addPiece function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException>{
            Board(8).addPiece(Coordinates(1,-1), value = PieceType.BLACK)
            Board(8).addPiece(Coordinates(1,0), value = PieceType.BLACK)
            Board(8).addPiece(Coordinates(1,27), value = PieceType.BLACK)
            Board(8).addPiece(Coordinates(1,'@'), value = PieceType.BLACK)
            Board(8).addPiece(Coordinates(1,'['), value = PieceType.BLACK)
        }
    }

    @Test
    fun `addPiece function with index outside range fails`() {
        assertFailsWith<IllegalArgumentException>{
            Board(8).addPiece(-1, PieceType.BLACK)
            Board(8).addPiece(64, PieceType.BLACK)
        }
    }

    @Test
    fun `addPiece function with piece already at position fails`() {
        assertFailsWith <IllegalArgumentException>{
            var uut = Board(4).addPiece(Coordinates(1, 'a'), PieceType.WHITE)
            uut = uut.addPiece(Coordinates(1, 'a'), PieceType.BLACK)
            var uut2 = Board(4).addPiece(5, PieceType.WHITE)
            uut2 = uut.addPiece(5, PieceType.BLACK)
            var uut3 = Board(4).addPiece(Coordinates(4, 2), PieceType.WHITE)
            uut3 = uut.addPiece(Coordinates(4, 2), PieceType.BLACK)
        }
    }
}