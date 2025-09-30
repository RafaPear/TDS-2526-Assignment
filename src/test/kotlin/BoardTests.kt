import kotlin.test.Test
import kotlin.test.assertFailsWith

import pt.isel.reversi.Board

class BoardTests {
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
    fun `Create Board with even side inside range succeeds`() {
        Board(6)
        Board(26)
        Board(16)
    }

    // Piece tests

    /*@Test
    fun `Create Piece with negative row fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board.Piece(row = -1, col = 1, 'w')
        }
    }

    @Test
    fun `Create Piece with negative col fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board.Piece(row = 1, col = -1, 'w')
        }
    }

    @Test
    fun `Create Piece with zero row fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board.Piece(row = 0, col = 1, 'w')
        }
    }

    @Test
    fun `Create Piece with zero col fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board.Piece(row = 1, col = 0, 'w')
        }
    }

    @Test
    fun `Create Piece with a third color fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board.Piece(row = 1, col = 0, 'y')
        }
    }

    @Test
    fun `Create Piece with positive row and col & valid color succeeds`() {
        Board.Piece(row = 1, col = 1, 'w')
    }*/

    // Board get functions tests

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

    // Board changePiece function tests

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

    // Board addPiece function tests

    /*@Test
    fun `addPiece function with row outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).addPiece(row = -1, col = 1, value = 'w')
            Board(8).addPiece(row = 0, col = 1, value = 'w')
            Board(8).addPiece(row = 27, col = 1, value = 'w')
        }
    }

    @Test
    fun `addPiece function with col outside range fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).addPiece(row = 1, col = -1, value = 'w')
            Board(8).addPiece(row = 1, col = 0, value = 'w')
            Board(8).addPiece(row = 1, col = 27, value = 'w')
            Board(8).addPiece(row = 1, col = '@', value = 'w')
            Board(8).addPiece(row = 1, col = '[', value = 'w')
        }
    }

    @Test
    fun `addPiece function with a third color fails`() {
        assertFailsWith<IllegalArgumentException> {
            Board(8).addPiece(row = 1, col = 1, value = 'y')
        }
    }

    @Test
    fun `addPiece function with valid Piece succeeds`() {
        Board(8).addPiece(row = 1, col = 1, value = 'w')
        Board(8).addPiece(row = 2, col = 1, value = 'b')
        Board(8).addPiece(row = 1, col = 2, value = 'b')
        Board(8).addPiece(row = 2, col = 2, value = 'w')
    }

    // Board startPieces function tests

    @Test
    fun `startPieces function adds the correct initial pieces succeeds`() {
        val startedBoard = Board(8).startPieces()
        val expectedPieces = listOf(
            Board.Piece(row = 4, col = 4, 'w'),
            Board.Piece(row = 5, col = 5, 'w'),
            Board.Piece(row = 4, col = 5, 'b'),
            Board.Piece(row = 5, col = 4, 'b')
        )
        assertEquals(expectedPieces, startedBoard)
    }*/

}
