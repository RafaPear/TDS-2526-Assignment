package pt.isel.reversi.core.game.localgda

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.GDAImpl
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.exceptions.InvalidGameWriteException
import java.io.File

/**
 * Local filesystem implementation of the game data access layer.
 *
 * Persists one game per text file using the line-oriented format handled by
 * [GameFileAccess]. This implementation focuses on deterministic, append-only
 * mutation for moves and passes while allowing full-file overwrites for snapshot
 * writes via [postGame]. IO exceptions from file operations are propagated to
 * callers.
 */
class LocalGDA : GDAImpl {

    /**
     * Appends a new piece placement to the persisted game file.
     *
     * @param fileName target game file path or identifier
     * @param piece the newly placed piece to record
     * @throws java.io.IOException when the underlying file I/O fails
     */
    override fun postPiece(fileName: String, piece: Piece) {
        GameFileAccess.writePiece(File(fileName), piece)
    }

    /**
     * Creates or updates the persisted game file with a full snapshot (headers and board).
     *
     * Behavior:
     * - If the target file does not exist or is empty, a new file is created and a full
     *   snapshot is written. The available pieces header is computed from `game.players`.
     * - If the file exists, its headers are read first. The persisted board side must match
     *   `game.board.side` or an [InvalidGameWriteException] is thrown to avoid corruption.
     *   The persisted `availablePieces` header is filtered by the players present in `game`
     *   and the file is overwritten with the updated snapshot.
     *
     * @param fileName target game file path or identifier
     * @param game the game state to persist
     * @throws java.io.IOException when the underlying file I/O fails
     * @throws InvalidGameWriteException when an existing file contains an incompatible side
     */
    override fun postGame(fileName: String, game: GameImpl) {
        val file = File(fileName)

        val isNewFile = !file.exists() || file.length() == 0L

        if (isNewFile) {
            file.createNewFile() // may throw IOException, let caller handle as IO_ERROR
            val players = game.players.map { it.type }
            val availablePieces = PieceType.entries.filter { it !in players }
            GameFileAccess.writeGame(file, availablePieces, game)
            return
        }

        val (availablePieces, side) = GameFileAccess.readGameInfo(file)

        val board = game.board ?: throw IllegalArgumentException("Game board is null")

        if (side != board.side) throw InvalidGameWriteException("Mismatched side in existing file")

        val availablePiecesData = availablePieces.filter { it !in game.players.map { p -> p.type } }

        GameFileAccess.writeGame(file, availablePiecesData, game)
    }

    /**
     * Records a pass turn (no board mutation) in the persisted game file.
     * Turn alternation is NOT enforced by this method; callers must validate turn rules.
     *
     * @param fileName target game file path or identifier
     * @param pieceType the piece type of the player passing their turn
     * @throws java.io.IOException when the underlying file I/O fails
     */
    override fun postPass(fileName: String, pieceType: PieceType) {
        GameFileAccess.writePass(File(fileName), pieceType)
    }

    /**
     * Reconstructs the [Board] solely from persisted piece lines and the header side.
     *
     * @param fileName target game file path or identifier
     * @return reconstructed [Board]
     * @throws java.io.IOException when the underlying file I/O fails
     * @throws pt.isel.reversi.core.game.exceptions.InvalidSideInFileException when the side header
     *         is missing or invalid
     * @throws pt.isel.reversi.core.game.exceptions.InvalidPieceInFileException when a piece line is malformed
     */
    override fun getBoard(fileName: String): Board = GameFileAccess.readBoard(File(fileName))

    /**
     * Returns remaining piece types not yet assigned to players, as declared in the file header.
     *
     * @param fileName target game file path or identifier
     * @return list of declared [PieceType] values (empty if none)
     * @throws java.io.IOException when the underlying file I/O fails
     */
    override fun getAvailablePieces(fileName: String): List<PieceType> =
        GameFileAccess.readAvailablePieces(File(fileName))

    /**
     * Returns the last serialized piece (chronological order).
     *
     * @param fileName target game file path or identifier
     * @return the last recorded [Piece]
     * @throws NoSuchElementException if there are no piece lines in the file
     * @throws java.io.IOException when the underlying file I/O fails
     */
    override fun getLatestPiece(fileName: String): Piece {
        val pieces = GameFileAccess.readPieces(File(fileName))
        return pieces.lastOrNull() ?: throw NoSuchElementException("No pieces found in file")
    }

    /**
     * Returns the last play type (piece or pass) from the chronology of play lines.
     *
     * @param fileName target game file path or identifier
     * @return the last recorded [PieceType]
     * @throws NoSuchElementException if there are no play lines in the file
     * @throws java.io.IOException when the underlying file I/O fails
     */
    override fun getLatestPlayType(fileName: String): PieceType {
        val pieceTypes = GameFileAccess.readPieceTypes(File(fileName))
        return pieceTypes.lastOrNull() ?: throw NoSuchElementException("No pieces found in file")
    }
}