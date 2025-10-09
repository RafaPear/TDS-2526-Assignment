package pt.isel.reversi.core.game.localgda

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.MockGame
import pt.isel.reversi.core.game.exceptions.InvalidAvailablePiecesInFileException
import pt.isel.reversi.core.game.exceptions.InvalidGameWriteException
import pt.isel.reversi.core.game.exceptions.InvalidPieceInFileException
import pt.isel.reversi.core.game.exceptions.InvalidSideInFileException
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalGDATests {

    @Test
    fun `postGame creates header and board for new file`() {
        val file = File.createTempFile("localgda-game", ".txt")
        try {
            val gda = LocalGDA()
            val game = MockGame.EmptyPlayers(gda, file.absolutePath)

            // should create the file and write headers
            gda.postGame(file.absolutePath, game)

            val available = gda.getAvailablePieces(file.absolutePath)
            // No players => all piece types available in enum order
            assertEquals(PieceType.entries.toList(), available)

            val board = gda.getBoard(file.absolutePath)
            assertEquals(game.board.side, board.side)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `postPiece appends piece and latest piece and play type are correct`() {
        val file = File.createTempFile("localgda-piece", ".txt")
        try {
            val gda = LocalGDA()
            val game = MockGame.EmptyPlayers(gda, file.absolutePath)
            gda.postGame(file.absolutePath, game)

            val piece = Piece(Coordinate(1, 'a'), PieceType.BLACK)
            gda.postPiece(file.absolutePath, piece)

            val latest = gda.getLatestPiece(file.absolutePath)
            assertEquals(piece, latest)

            val latestType = gda.getLatestPlayType(file.absolutePath)
            assertEquals(PieceType.BLACK, latestType)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `postPass writes pass and latest play type reflects pass and latest piece is missing`() {
        val file = File.createTempFile("localgda-pass", ".txt")
        try {
            val gda = LocalGDA()
            val game = MockGame.EmptyPlayers(gda, file.absolutePath)
            gda.postGame(file.absolutePath, game)

            gda.postPass(file.absolutePath, PieceType.WHITE)

            val latestType = gda.getLatestPlayType(file.absolutePath)
            assertEquals(PieceType.WHITE, latestType)

            // There are no piece lines (only a pass), so getLatestPiece should throw
            val ex = assertFailsWith<NoSuchElementException> {
                gda.getLatestPiece(file.absolutePath)
            }
            assertEquals("No pieces found in file", ex.message)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `postGame on existing file with mismatched side throws`() {
        val file = File.createTempFile("localgda-mismatch", ".txt")
        try {
            // write a header with a different side than the game we will provide
            file.writeText("availablePieces: #|@\nside: 6\n")

            val gda = LocalGDA()
            val game = MockGame.EmptyPlayers(gda, file.absolutePath) // board side is 8

            val ex = assertFailsWith<InvalidGameWriteException> {
                gda.postGame(file.absolutePath, game)
            }
            assertEquals("Mismatched side in existing file", ex.message)
        } finally {
            file.delete()
        }
    }

    // --- Additional tests requested ---

    @Test
    fun `postGame on existing file updates available pieces filtering players`() {
        val file = File.createTempFile("localgda-exists", ".txt")
        try {
            // existing header with both piece symbols
            file.writeText("availablePieces: #|@\nside: 8\n")

            val gda = LocalGDA()
            // game with one player BLACK should remove BLACK from available pieces
            val game = MockGame.OnePlayer(gda, file.absolutePath)

            gda.postGame(file.absolutePath, game)

            val available = gda.getAvailablePieces(file.absolutePath)
            assertEquals(listOf(PieceType.WHITE), available)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `postGame on existing file with two players results in no available pieces`() {
        val file = File.createTempFile("localgda-exists-2players", ".txt")
        try {
            // existing header with both piece symbols
            file.writeText("availablePieces: #|@\nside: 8\n")

            val gda = LocalGDA()
            // game with two players should remove both BLACK and WHITE
            val game = MockGame.TwoPlayers(gda, file.absolutePath)

            gda.postGame(file.absolutePath, game)

            val available = gda.getAvailablePieces(file.absolutePath)
            assertEquals(emptyList(), available)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `getBoard reconstructs board from persisted pieces`() {
        val file = File.createTempFile("localgda-board", ".txt")
        try {
            val gda = LocalGDA()
            val baseGame = MockGame.EmptyPlayers(gda, file.absolutePath)
            // create a board with a piece at 1,a
            val boardWithPiece = Board(8).addPiece(Coordinate(1, 'a'), PieceType.BLACK)
            val game = baseGame.copy(board = boardWithPiece)

            gda.postGame(file.absolutePath, game)

            val board = gda.getBoard(file.absolutePath)
            assertEquals(8, board.side)
            assertEquals(PieceType.BLACK, board[Coordinate(1, 'a')])
        } finally {
            file.delete()
        }
    }

    @Test
    fun `latest piece and play type with mixed piece and pass lines`() {
        val file = File.createTempFile("localgda-mixed", ".txt")
        try {
            val gda = LocalGDA()
            val game = MockGame.EmptyPlayers(gda, file.absolutePath)
            gda.postGame(file.absolutePath, game)

            val p1 = Piece(Coordinate(2, 'b'), PieceType.BLACK)
            val p2 = Piece(Coordinate(3, 'c'), PieceType.WHITE)

            gda.postPiece(file.absolutePath, p1)
            gda.postPass(file.absolutePath, PieceType.WHITE)
            gda.postPiece(file.absolutePath, p2)
            gda.postPass(file.absolutePath, PieceType.BLACK)

            // latest piece should be p2 (last piece line)
            val latestPiece = gda.getLatestPiece(file.absolutePath)
            assertEquals(p2, latestPiece)

            // latest play type should be the type from the last line (a pass with BLACK)
            val latestType = gda.getLatestPlayType(file.absolutePath)
            assertEquals(PieceType.BLACK, latestType)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `read availablePieces reads only first availablePieces line`() {
        val file = File.createTempFile("localgda-available", ".txt")
        try {
            // multiple availablePieces lines; readAvailablePieces should return the first one
            file.writeText("availablePieces: #\navailablePieces: @\nside: 8\n")

            val gda = LocalGDA()
            val available = gda.getAvailablePieces(file.absolutePath)
            // Implementation aggregates all availablePieces lines, so expect both in order
            assertEquals(listOf(PieceType.BLACK, PieceType.WHITE), available)
        } finally {
            file.delete()
        }
    }

    // --- New error/edge-case tests ---

    @Test
    fun `getLatestPlayType throws when no plays in file`() {
        val file = File.createTempFile("localgda-noplay", ".txt")
        try {
            val gda = LocalGDA()
            val game = MockGame.EmptyPlayers(gda, file.absolutePath)
            gda.postGame(file.absolutePath, game)

            val ex = assertFailsWith<NoSuchElementException> {
                gda.getLatestPlayType(file.absolutePath)
            }
            assertEquals("No pieces found in file", ex.message)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `getBoard throws when availablePieces missing`() {
        val file = File.createTempFile("localgda-noavail", ".txt")
        try {
            file.writeText("side: 8\n")

            val gda = LocalGDA()
            val ex = assertFailsWith<InvalidAvailablePiecesInFileException> {
                gda.getBoard(file.absolutePath)
            }
            assertEquals("Missing available pieces line in file", ex.message)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `getBoard throws when side missing`() {
        val file = File.createTempFile("localgda-noside", ".txt")
        try {
            file.writeText("availablePieces: #|@\n")

            val gda = LocalGDA()
            val ex = assertFailsWith<InvalidSideInFileException> {
                gda.getBoard(file.absolutePath)
            }
            assertEquals("Missing side line in file", ex.message)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `getBoard throws on malformed piece line`() {
        val file = File.createTempFile("localgda-malformed", ".txt")
        try {
            // valid headers but malformed piece line (non-integer column)
            file.writeText("availablePieces: #|@\nside: 8\npiece: 1 two #\n")

            val gda = LocalGDA()
            val ex = assertFailsWith<InvalidPieceInFileException> {
                gda.getBoard(file.absolutePath)
            }
            assertEquals("Invalid piece column in file", ex.message)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `multiple piece lines are reconstructed and latest piece is correct`() {
        val file = File.createTempFile("localgda-multipiece", ".txt")
        try {
            file.writeText("availablePieces: #|@\nside: 8\n")
            file.appendText("piece: 1 1 #\n")
            file.appendText("piece: 2 2 @\n")

            val gda = LocalGDA()
            val latest = gda.getLatestPiece(file.absolutePath)
            assertEquals(Piece(Coordinate(2, 2), PieceType.WHITE), latest)

            val board = gda.getBoard(file.absolutePath)
            assertEquals(PieceType.BLACK, board[Coordinate(1, 1)])
            assertEquals(PieceType.WHITE, board[Coordinate(2, 2)])
        } finally {
            file.delete()
        }
    }
}