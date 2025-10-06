package pt.isel.reversi.core.game.localgda

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.data.GDACodes
import pt.isel.reversi.core.game.data.GDAImpl
import pt.isel.reversi.core.game.data.GDAResult
import java.io.File

/**
 * Local (filesystem) implementation of [GDAImpl] that persists game snapshots and move history as
 * a plain UTF‑8 line oriented text file. Chosen goals: human readable, append friendly, stable,
 * and simple to diff under version control during development.
 *
 * File layout (logical sections) – required line ordering for headers:
 *  1. availablePieces: SYMBOL('|SYMBOL')*            (may be empty after the colon)
 *  2. side: N                                        (even integer 4..26)
 *  3+. player: SYMBOL points playsLeft               (0..2 lines currently supported)
 *  --- dynamic history ---
 *  piece: row col SYMBOL                             (1‑based row/col indexes)
 *  pass: SYMBOL                                      (record of a pass turn)
 *
 * Invariants / validation strategy:
 *  - Header must include the first two lines; player lines are optional (0..2)
 *  - 'availablePieces' lists the piece symbols NOT yet chosen by players (initial provisioning)
 *  - Board side must be within allowed domain (even & bounded)
 *  - History reconstruction only considers valid 'piece:' lines; malformed ones produce BOARD_ERROR
 *  - Method calls never throw for expected domain issues; they return [GDAResult] with a suitable [GDACodes]
 *
 * Typical lifecycle:
 *  1. Create (or reuse) a filename and call [postGame] with an initialized board & players
 *  2. For every accepted move, call [postPiece]
 *  3. For a skipped turn, call [postPass]
 *  4. To rebuild state (e.g. spectator / resume) call [getBoard] and optionally [getLatestPiece]
 *
 * Concurrency note: This implementation is NOT synchronized. Concurrent writers to the same file
 * may interleave content. Introduce file locks or an abstraction for multi‑process safety if needed.
 *
 * Error surfaces (selected):
 *  - DATA_NOT_FOUND : missing / unreadable file on read‑oriented operations
 *  - IO_ERROR       : underlying IOException or unexpected runtime issue
 *  - SIDE_ERROR     : invalid or missing board side declaration
 *  - AVAILABLE_PIECES_ERROR : absence of the available pieces header when requested
 *  - BOARD_ERROR    : malformed piece serialization during latest piece extraction
 */
class LocalGDA : GDAImpl {

    private val availablePiecesPrefix = "availablePieces:"
    private val sidePrefix = "side:"
    private val piecePrefix = "piece:"
    private val passPrefix = "pass:"
    private val playerPrefix = "player:"

    /**
     * Resolves an existing writable file. Returns DATA_NOT_FOUND if it doesn't exist or is not a
     * regular writable file (keeps higher level logic simpler & consistent).
     */
    private fun getFile(fileName: String): GDAResult<File> {
        val file = File(fileName)
        return if (file.exists() && file.isFile && file.canWrite())
            GDACodes.SUCCESS(LocalGDAMessages.fileReadSuccess(fileName), file)
        else
            GDACodes.DATA_NOT_FOUND(LocalGDAMessages.fileNotFound(fileName), null)
    }

    /**
     * Loads all lines from file, enforcing non‑emptiness. Used by read operations to unify error
     * mapping. Returns DATA_NOT_FOUND if empty to signal absence of persisted content.
     */
    private fun getLines(fileName: String): GDAResult<List<String>> {
        val result = getFile(fileName)
        val file = result.data ?: return result.toOtherType()
        return try {
            val lines = file.readLines()
            if (lines.isEmpty())
                GDACodes.DATA_NOT_FOUND(LocalGDAMessages.fileEmpty(fileName), null)
            else
                GDACodes.SUCCESS(LocalGDAMessages.fileReadSuccess(fileName), lines)
        } catch (e: Exception) {
            GDACodes.IO_ERROR(LocalGDAMessages.fileReadError(fileName, e), null)
        }
    }

    /** Parses a serialized piece line or returns null if malformed (caller decides error surface). */
    private fun parsePiece(line: String): Piece? =
        line.removePrefix(piecePrefix).trim().split(" ").takeIf { it.size == 3 }?.let {
            val (r, c, s) = it
            val row = r.toIntOrNull() ?: return null
            val col = c.toIntOrNull() ?: return null
            val type = PieceType.fromSymbol(s.firstOrNull() ?: return null) ?: return null
            Piece(Coordinate(row, col), type)
        }

    /** Extracts board side integer, or null if not a valid integer token. */
    private fun parseSide(line: String): Int? =
        line.removePrefix(sidePrefix).trim().toIntOrNull()

    /**
     * Appends a new piece placement to history.
     * @return SUCCESS(true) on append; DATA_NOT_FOUND(false) if file missing; IO_ERROR(false) otherwise.
     */
    override fun postPiece(fileName: String, piece: Piece) = try {
        val file = getFile(fileName).data ?: return GDACodes.DATA_NOT_FOUND(
            LocalGDAMessages.fileNotFound(fileName),
            false
        )
        file.appendText("$piecePrefix ${piece.coordinate.row} ${piece.coordinate.col} ${piece.value.symbol}\n")
        GDACodes.SUCCESS(LocalGDAMessages.pieceSaved(fileName, piece), true)
    } catch (e: Exception) {
        GDACodes.IO_ERROR(LocalGDAMessages.fileWriteError(fileName, e), false)
    }

    /**
     * Creates or overwrites a game file with headers & existing board snapshot, then replays each
     * existing board piece as a history line for uniform reconstruction.
     * Header semantics: players present reduce 'availablePieces' list.
     * @return SUCCESS(true) on write; IO_ERROR(false) on failure.
     */
    override fun postGame(fileName: String, game: GameImpl) = try {
        val file = getFile(fileName).data ?: File(fileName).apply { createNewFile() }
        val missing = PieceType.entries.map { it.symbol } - game.players.map { it.type.symbol }.toSet()
        val players = game.players.joinToString("\n") { "$playerPrefix ${it.type.symbol} ${it.points} ${it.playsLeft}" }

        file.writeText(
            "$availablePiecesPrefix ${missing.joinToString("|")}\n" +
            "$sidePrefix ${game.board.side}\n" +
            "$players\n"
        )
        game.board.forEach { postPiece(fileName, it) }
        GDACodes.SUCCESS(LocalGDAMessages.gameWritten(fileName), true)
    } catch (e: Exception) {
        GDACodes.IO_ERROR(LocalGDAMessages.fileWriteError(fileName, e), false)
    }

    /**
     * Records a pass turn (no board mutation). Turn alternation is NOT enforced here; callers may
     * implement higher level validation if required.
     */
    override fun postPass(fileName: String, pieceType: PieceType): GDAResult<Boolean> {
        val file = getFile(fileName).data ?: return GDACodes.DATA_NOT_FOUND(
            LocalGDAMessages.fileNotFound(fileName),
            false
        )
        file.appendText("$passPrefix ${pieceType.symbol}\n")
        return GDACodes.SUCCESS(LocalGDAMessages.passRecorded(fileName, pieceType), true)
    }

    /**
     * Reconstructs the board solely from persisted piece lines. Side validation occurs before
     * piece parsing. Malformed piece lines are ignored during board rebuild (only latest piece
     * extraction reports BOARD_ERROR).
     */
    override fun getBoard(fileName: String): GDAResult<Board> {
        return try {
            val lines = getLines(fileName).data ?: return GDACodes.DATA_NOT_FOUND(
                LocalGDAMessages.fileEmpty(fileName),
                null
            )
            val side = lines.firstNotNullOfOrNull { if (it.startsWith(sidePrefix)) parseSide(it) else null }
                       ?: return GDACodes.SIDE_ERROR(LocalGDAMessages.missingSide(fileName), null)

            if (side !in 4..26 || side % 2 != 0)
                return GDACodes.SIDE_ERROR(LocalGDAMessages.invalidSide(fileName), null)

            val pieces = lines.filter { it.startsWith(piecePrefix) }.mapNotNull { parsePiece(it) }
            GDACodes.SUCCESS(LocalGDAMessages.boardLoaded(fileName), Board(side, pieces))
        } catch (e: Exception) {
            GDACodes.IO_ERROR(LocalGDAMessages.fileReadError(fileName, e), null)
        }
    }

    /**
     * Returns remaining piece types (by symbol) not yet taken by players. Empty list means all
     * piece types already assigned.
     */
    override fun getAvailablePieces(fileName: String): GDAResult<List<PieceType>> {
        val lines = getLines(fileName).data ?: return GDACodes.DATA_NOT_FOUND(
            LocalGDAMessages.fileEmpty(fileName),
            null
        )
        val line = lines.firstOrNull()?.takeIf { it.startsWith(availablePiecesPrefix) }
                   ?: return GDACodes.AVAILABLE_PIECES_ERROR(LocalGDAMessages.availablePiecesMissing(fileName), null)

        val symbols = line.removePrefix(availablePiecesPrefix).trim().split("|").filter { it.isNotEmpty() }
        val pieces = symbols.mapNotNull { PieceType.fromSymbol(it.first()) }
        return GDACodes.SUCCESS(LocalGDAMessages.fileReadSuccess(fileName), pieces)
    }

    /**
     * Returns the last serialized piece (chronological order). Returns SUCCESS(null) if no piece
     * lines exist. BOARD_ERROR is returned only if the final matching line cannot be parsed.
     */
    override fun getLatestPiece(fileName: String): GDAResult<Piece?> {
        val lines = getLines(fileName).data ?: return GDACodes.DATA_NOT_FOUND(
            LocalGDAMessages.fileEmpty(fileName),
            null
        )
        val line = lines.asReversed().firstOrNull { it.startsWith(piecePrefix) }
                   ?: return GDACodes.SUCCESS(LocalGDAMessages.noPiecesFound(fileName), null)

        val piece = parsePiece(line)
                    ?: return GDACodes.BOARD_ERROR(LocalGDAMessages.invalidPiece(), null)

        return GDACodes.SUCCESS(LocalGDAMessages.latestPieceFound(fileName, piece), piece)
    }
}
