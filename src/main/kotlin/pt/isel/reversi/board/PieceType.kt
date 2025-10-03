package pt.isel.reversi.board

/**
 * Represents the type of piece on the board.
 */
enum class PieceType(val symbol: Char) {
    BLACK('#'),
    WHITE('@');

    fun swap(): PieceType =
        if (this == BLACK) WHITE
        else BLACK
}
