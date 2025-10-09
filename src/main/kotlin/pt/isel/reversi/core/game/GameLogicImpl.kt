package pt.isel.reversi.core.game

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType

/**
 * Interface for game logic operations in Reversi.
 */
@Suppress("unused")
interface GameLogicImpl {
    /**
     * Plays a move on the board at the specified row and column.
     * @param board The current board state.
     * @param row The row to play.
     * @param col The column to play.
     * @return The new board state after the move, or null if invalid.
     */
    fun play(board: Board, row: Int, col: Int): Board?

    /**
     * Gets the available plays for a given piece type on the board.
     * @param board The current board state.
     * @param piece The piece type to check for available moves.
     * @return A list of available (row, column) pairs.
     */
    fun getAvailablePlays(board: Board, piece: PieceType): List<Pair<Int, Int>>

    // TODO FAZER RESTOOOOOOOOOOOOOO
}