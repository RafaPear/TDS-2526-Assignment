package pt.isel.reversi.core.game

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType

/**
 * Contract for persistence operations related to a Reversi game.
 *
 * Implementations of this interface are responsible for storing and loading the
 * textual representation of a game's state (headers, piece history and pass lines).
 * Implementations may use the local filesystem, a database, or a remote service.
 *
 * Note: IO failures from the underlying storage are surfaced as standard IOExceptions
 * from implementation code; callers should treat those as transient I/O errors.
 */
interface GDAImpl {

    /**
     * Persist a newly placed [piece] into the game storage identified by [fileName].
     *
     * @param fileName path or identifier of the game resource
     * @param piece the piece placement to record (row/col and piece type)
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     */
    fun postPiece(fileName: String, piece: Piece)

    /**
     * Persist the full game snapshot (players, board side and pieces) under [fileName].
     * Implementations may create a new resource or overwrite an existing one.
     *
     * @param fileName path or identifier of the game resource
     * @param game the game state to persist
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     */
    fun postGame(fileName: String, game: GameImpl)

    /**
     * Record a pass action for the player of [pieceType] in the game resource.
     *
     * @param fileName path or identifier of the game resource
     * @param pieceType the piece type of the player that passed their turn
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     */
    fun postPass(fileName: String, pieceType: PieceType)

    // GET METHODS

    /**
     * Load the current board reconstructed from persisted piece history under [fileName].
     *
     * @param fileName path or identifier of the game resource
     * @return the reconstructed [Board]
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     * @throws pt.isel.reversi.core.game.exceptions.InvalidSideInFileException if the persisted
     *         side header is missing/invalid
     * @throws pt.isel.reversi.core.game.exceptions.InvalidPieceInFileException if a persisted
     *         piece line is malformed
     */
    fun getBoard(fileName: String): Board

    /**
     * Retrieve the available piece types declared in the game's header.
     *
     * @param fileName path or identifier of the game resource
     * @return list of remaining/declared [PieceType] values (empty if none)
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     */
    fun getAvailablePieces(fileName: String): List<PieceType>

    /**
     * Get the last placed piece (chronologically) recorded in the game resource.
     *
     * @param fileName path or identifier of the game resource
     * @return the last recorded [Piece]
     * @throws NoSuchElementException if no piece lines exist in the persisted resource
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     */
    fun getLatestPiece(fileName: String): Piece

    /**
     * Get the last play type (piece or pass) from the persisted chronology.
     *
     * @param fileName path or identifier of the game resource
     * @return the last recorded [PieceType]
     * @throws NoSuchElementException if there are no play lines in the persisted resource
     * @throws java.io.IOException on underlying I/O errors from the persistence layer
     */
    fun getLatestPlayType(fileName: String): PieceType

}