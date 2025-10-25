package pt.isel.reversi.core.game

import pt.isel.reversi.core.Environment.BOARD_SIDE
import pt.isel.reversi.core.Environment.firstPlayerTurn
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType

/**
 * Interface for the main game operations and state in Reversi.
 */
interface GameImpl {
    /** Data access layer for game persistence. */
    val dataAccess: GDAImpl

    /** List of players in the game. */
    val players: List<Player>

    /** The current game name, if any. */
    val currGameName: String?

    /** The current board state. */
    val board: Board?

    /** Indicates if target mode is enabled. */
    val target: Boolean

    /** Index of the next player's turn. */
    val playerTurn: PieceType
    /**
     * Plays a move at the specified row and column.
     * @param coordinate The (row, column) coordinate for the move.
     * @return The new game state after the move, or null if invalid.
     */
    fun play(coordinate: Coordinate): GameImpl

    /**
     * Gets the available piece options for a player name.
     * @return List of available piece types.
     */
    fun pieceOptions(): List<PieceType>

    /**
     * Sets the target mode for the game.
     * @param target True to enable target mode.
     * @return The updated game state.
     */
    fun setTargetMode(target: Boolean): GameImpl

    /**
     * Gets the available plays for the current player.
     * @return List of available (row, column) pairs.
     */
    fun getAvailablePlays(): List<Coordinate>

    /**
     * Starts a new game.
     */
    fun startNewGame(
        side: Int = BOARD_SIDE,
        players: List<Player>,
        firstTurn: PieceType = firstPlayerTurn,
        currGameName: String? = null,
        ): GameImpl

    /**
     * Passes the current turn.
     */
    fun pass() : GameImpl

    /**
     * Refreshes the game state.
     * @return The refreshed game state.
     */
    fun refresh(): GameImpl

    /**
     * Returns the current board state.
     * @return The board.
     */
    fun poopBoard(): Board?

    fun copy(
        dataAccess: GDAImpl = this.dataAccess,
        players: List<Player> = this.players,
        target: Boolean = this.target,
        playerTurn: PieceType = this.playerTurn,
        currGameName: String? = this.currGameName,
        board: Board? = this.board,
    ): GameImpl
}