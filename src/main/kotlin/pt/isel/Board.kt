package pt.isel

const val SIDE_MIN = 4
const val SIDE_MAX = 26

/**
 * Represents a board game grid.
 *
 * @property Piece The piece on the board, either 'b' for black or 'w' for white.
 * @property side Number of rows and columns of the board.
 */
class Board(private val side: Int) {
    private val pieces: List<Piece> = listOf()

    init {
        require(side in SIDE_MIN..SIDE_MAX) {
            "Side must be between $SIDE_MIN and $SIDE_MAX"
        }
        require(side % 2 == 0) {
            "Side must be an even number"
        }
        val mid = side / 2
        pieces.plus(Piece(mid, mid, 'w'))
        pieces.plus(Piece(mid + 1, mid + 1, 'w'))
        pieces.plus(Piece(mid, mid + 1, 'b'))
        pieces.plus(Piece(mid + 1, mid, 'b'))
    }
    /**
     * Represents a piece on the board.
     */
    data class Piece(val row: Int, val col: Int, var value: Char) {
        init {
            val char = value.lowercase()[0]
            require(row in 1..SIDE_MAX) {
                "Row must be between 1 and $SIDE_MAX"
            }
            require(col in 1..SIDE_MAX) {
                "Column must be between 1 and $SIDE_MAX"
            }
            require(char == 'b' || char == 'w') {
                "Value must be 'b' or 'w'"
            }
        }
    }

    /**
     * Gets the piece at the specified row and column.
     * @return The piece at the specified position, or null if there is no piece.
     */
    operator fun get(row: Int, col: Char): Piece? = this[row, charIndexToInt(col)]

    /**
     * Gets the piece at the specified row and column.
     * @return The piece at the specified position, or null if there is no piece.
     */
    operator fun get(row: Int, col: Int): Piece? {
        require(row in 1..side) {
            "Row must be between 1 and $side" }
        require(col in 1..side) {
            "Column must be between 1 and $side"
        }
        return pieces.find { it.row == row && it.col == col }
    }

    /**
     * Changes the piece at the specified row and column from 'b' to 'w' or from 'w' to 'b'.
     * @return true if the piece was changed, false if there is no piece at the specified position.
     */
    fun changePiece(row: Int, col: Char): Boolean = changePiece(row, charIndexToInt(col))

    /**
     * Changes the piece at the specified row and column from 'b' to 'w' or from 'w' to 'b'.
     * @return true if the piece was changed, false if there is no piece at the specified position.
     */
    fun changePiece(row: Int, col: Int): Boolean {
        require(row in 1..side) {
            "Row must be between 1 and $side" }
        require(col in 1..side) {
            "Column must be between 1 and $side"
        }
        val value = this[row,col]?.value ?: return false
        val newValue = if (value == 'b') 'w' else 'b'
        this[row,col]?.value = newValue
        return true
    }

    /**
     * Adds a piece to the board at the specified row and column.
     */
    fun addPiece(row: Int, col: Char, value: Char) = addPiece(row, charIndexToInt(col), value)

    /**
     * Adds a piece to the board at the specified row and column.
     */
    fun addPiece(row: Int, col: Int, value: Char) {
        val value = value.lowercase()[0]

        require(row in 1..side) {
            "Row must be between 1 and $side"
        }
        require(col in 1..side) {
            "Column must be between 1 and $side"
        }
        require(value == 'b' || value == 'w') {
            "Value must be 'b' or 'w'"
        }

        pieces.plus(Piece(row, col, value))
    }

    /**
     * Converts a column character ('a', 'b', ...) to its corresponding integer index.
     */
    private fun charIndexToInt(col: Char): Int {
        val colLower = col.lowercase()[0]
        require(colLower in 'a'..'a' + side - 1) {
            "Column must be between 'a' and '${'a' + side - 1}'" }
        return colLower - 'a' + 1
    }
}