package pt.isel.reversi.board

/**
 * Represents a piece on the board.
 */
data class Piece(
    val row: Int,
    val col: Int,
    val value: PieceType
)