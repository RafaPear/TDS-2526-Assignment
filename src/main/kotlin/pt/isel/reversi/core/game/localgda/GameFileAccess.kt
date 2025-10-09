package pt.isel.reversi.core.game.localgda

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.exceptions.InvalidAvailablePiecesInFileException
import pt.isel.reversi.core.game.exceptions.InvalidPieceInFileException
import pt.isel.reversi.core.game.exceptions.InvalidSideInFileException
import java.io.File

/**
 * Low-level helpers to read/write the textual persistence format used by [LocalGDA].
 *
 * The format is line-oriented and intended to be human-readable. This object exposes
 * small, well-documented functions that handle serialization and parsing of the
 * different line kinds: available pieces, side, piece placements and pass turns.
 */
object GameFileAccess {
    const val AVAILABLE_PIECES_PREFIX = "availablePieces:"
    const val SIDE_PREFIX = "side:"
    const val PIECE_PREFIX = "piece:"
    const val PASS_PREFIX = "pass:"

    /**
     * Parses a serialized piece line and returns a [Piece].
     * Expected line format: "piece: <row> <col> <symbol>" where <row> and <col> are integers
     * and <symbol> is a single character representing a [PieceType].
     *
     * @param line the input line to parse
     * @return the parsed [Piece]
     * @throws InvalidPieceInFileException when the line is malformed or contains invalid values
     */
    private fun parsePiece(line: String): Piece {
        val piece = line.removePrefix(PIECE_PREFIX).trim().split(" ").takeIf { it.size == 3 }
                    ?: throw InvalidPieceInFileException("Piece line does not have exactly 3 components")

        val (r, c, s) = piece
        val row = r.toIntOrNull() ?: throw InvalidPieceInFileException("Invalid piece row in file")
        val col = c.toIntOrNull() ?: throw InvalidPieceInFileException("Invalid piece column in file")
        val symbol = s.firstOrNull() ?: throw InvalidPieceInFileException("Invalid piece type in file")
        val type = PieceType.fromSymbol(symbol) ?: throw InvalidPieceInFileException("Unknown piece type in file")
        return Piece(Coordinate(row, col), type)
    }

    /**
     * Extracts board side integer from a side line.
     * Expected format: "side: <int>".
     *
     * @param line the input side line
     * @return the parsed side as an Int
     * @throws InvalidSideInFileException when the token cannot be parsed as Int
     */
    private fun parseSide(line: String): Int {
        val side = line.removePrefix(SIDE_PREFIX).trim().toIntOrNull()
        return side ?: throw InvalidSideInFileException()
    }

    /**
     * Parses an availablePieces line into a list of [PieceType].
     * Expected format: "availablePieces: SYMBOL(|SYMBOL)*" where SYMBOL is a single char.
     *
     * @param line the availablePieces line
     * @return list of parsed [PieceType]
     */
    private fun parseAvailablePieces(line: String): List<PieceType> {
        val data = line.removePrefix(AVAILABLE_PIECES_PREFIX).trim().split("|")
        val pieces = data.mapNotNull {
            val symbol = it.trim().firstOrNull()
            if (symbol != null) PieceType.fromSymbol(symbol)
            else null
        }
        return pieces
    }

    private fun writeGameUtil(file: File, line1: String, game: GameImpl) {
        val sideLine = "$SIDE_PREFIX ${game.board.side}\n"
        file.writeText(line1 + sideLine)
        game.board.forEach { writePiece(file, it) }
    }

    /**
     * Appends a serialized piece line to [file].
     * Format written: "piece: <row> <col> <symbol>\n".
     *
     * @param file target file
     * @param piece piece to serialize
     * @throws java.io.IOException when the underlying filesystem operation fails
     */
    fun writePiece(file: File, piece: Piece) {
        val line = "$PIECE_PREFIX ${piece.coordinate.row} ${piece.coordinate.col} ${piece.value.symbol}\n"
        file.appendText(line) // may throw IOException, let caller handle as IO_ERROR
    }

    /**
     * Appends a pass line to [file].
     * Format written: "pass: <symbol>\n".
     *
     * @param file target file
     * @param pieceType piece type that passed
     * @throws java.io.IOException when the underlying filesystem operation fails
     */
    fun writePass(file: File, pieceType: PieceType) {
        val line = "$PASS_PREFIX ${pieceType.symbol}\n"
        file.appendText(line) // may throw IOException, let caller handle as IO_ERROR
    }

    /**
     * Writes the full game header (available pieces + side) and the current board snapshot.
     * This overwrites the file content.
     *
     * @param file target file to write
     * @param availablePiecesData available piece types to record in header
     * @param game the game snapshot to write
     * @throws java.io.IOException when the underlying filesystem operation fails
     */
    fun writeGame(file: File, availablePiecesData: List<PieceType>, game: GameImpl) {
        val stringifyPieces = availablePiecesData.map { it.symbol }.joinToString("|")
        val availablePiecesLine = "$AVAILABLE_PIECES_PREFIX $stringifyPieces\n"
        writeGameUtil(file, availablePiecesLine, game)
    }

    /**
     * Reads game header information (available pieces and side) from the provided file.
     * The function scans the file for header lines and returns the first occurrences.
     *
     * @param file the file to scan
     * @return Pair(list of available pieces, board side)
     * @throws InvalidPieceInFileException if a piece line is malformed while scanning
     * @throws InvalidSideInFileException if the side line is missing or invalid
     * @throws InvalidAvailablePiecesInFileException if the available pieces line is missing
     */
    fun readGameInfo(file: File): Pair<List<PieceType>, Int> {
        var availablePieces: List<PieceType>? = null
        var side: Int? = null

        file.forEachLine { line ->
            when {
                line.startsWith(AVAILABLE_PIECES_PREFIX) -> availablePieces = parseAvailablePieces(line)
                line.startsWith(SIDE_PREFIX)             -> side = parseSide(line)
                else                                     -> return@forEachLine
            }
        }

        if (side == null) throw InvalidSideInFileException("Missing side line in file")
        if (availablePieces == null) throw InvalidAvailablePiecesInFileException("Missing available pieces line in file")

        return availablePieces to side
    }

    /**
     * Reads all piece lines in the file and returns them in chronological order.
     *
     * @param file the file to read
     * @return list of parsed [Piece] instances in file order
     * @throws InvalidPieceInFileException when a piece line is malformed
     */
    fun readPieces(file: File): List<Piece> {
        val pieces = mutableListOf<Piece>()

        file.forEachLine { line ->
            if (line.startsWith(PIECE_PREFIX)) {
                val piece = parsePiece(line)
                pieces.add(piece)
            }
        }
        return pieces
    }

    /**
     * Reads both piece and pass lines, returning the sequence of [PieceType] representing
     * the chronological plays (pieces and passes interleaved). Malformed pass lines are ignored.
     *
     * @param file the file to read
     * @return list of [PieceType] in chronological play order
     */
    fun readPieceTypes(file: File): List<PieceType> {
        val pieceTypes = mutableListOf<PieceType>()
        file.forEachLine { line ->
            if (line.startsWith(PIECE_PREFIX)) {
                val piece = parsePiece(line)
                pieceTypes.add(piece.value)
            }
            else if (line.startsWith(PASS_PREFIX)) {
                val symbol = line.removePrefix(PASS_PREFIX).trim().firstOrNull()
                if (symbol != null) {
                    val type = PieceType.fromSymbol(symbol)
                    if (type != null) {
                        pieceTypes.add(type)
                    }
                }
            }
        }
        return pieceTypes
    }

    /**
     * Returns the list of available pieces declared in the file. Aggregates the first
     * availablePieces line found in file order.
     *
     * @param file the file to read
     * @return list of declared [PieceType] (empty if none)
     * @throws InvalidAvailablePiecesInFileException when the available pieces header is missing
     */
    fun readAvailablePieces(file: File): List<PieceType> {
        val availablePieces = mutableListOf<PieceType>()
        file.forEachLine { line ->
            if (line.startsWith(AVAILABLE_PIECES_PREFIX)) {
                val pieces = parseAvailablePieces(line)
                availablePieces.addAll(pieces)
                return@forEachLine
            }
        }
        return availablePieces
    }

    /**
     * Reconstructs a [Board] instance from the file header (side) and the serialized piece lines.
     *
     * @param file the file to read
     * @return reconstructed [Board]
     * @throws InvalidSideInFileException when the side header is missing or invalid
     * @throws InvalidAvailablePiecesInFileException when the available pieces header is missing
     * @throws InvalidPieceInFileException when a piece line is malformed
     */
    fun readBoard(file: File): Board {
        val side = readGameInfo(file).second
        val pieces = readPieces(file)
        return Board(side, pieces)
    }
}