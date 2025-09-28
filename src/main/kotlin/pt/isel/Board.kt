package pt.isel


const val SIDE_MIN = 4
const val SIDE_MAX = 26

/**
 * Represents a board game grid.
 *
 * @property Piece The piece on the board, either 'b' for black or 'w' for white.
 * @property rows The number of rows on the board.
 * @property cols The number of columns on the board.
 */
data class Board(private val rows: Int,
                private val cols: Int,
                private val  pieces: List<Piece> = emptyList()
) {

    constructor(row: Int) : this(row, row)

    init {
        require(rows in SIDE_MIN..SIDE_MAX) {
            "Row must be between $SIDE_MIN and $SIDE_MAX"
        }
        require(cols in SIDE_MIN..SIDE_MAX) {
            "Column must be between $SIDE_MIN and $SIDE_MAX"
        }
        require(rows % 2 == 0 && cols % 2 == 0) {
            "Row must be even"
        }
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
        require(row in 1..rows) {
            "Row must be between 1 and $rows" }
        require(col in 1..cols) {
            "Column must be between 1 and $cols"
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
        require(row in 1..rows) {
            "Row must be between 1 and $rows"
        }
        require(col in 1..cols) {
            "Column must be between 1 and $cols}"
        }
        val value = this[row,col]?.value ?: return false
        val newValue = if (value == 'b') 'w' else 'b'
        this[row,col]?.value = newValue
        return true
    }

    /**
     * Adds a piece to the board at the specified row and column.
     */
    fun addPiece(row: Int, col: Char, value: Char): Board = this.addPiece(row, charIndexToInt(col), value)

    /**
     * Adds a piece to the board at the specified row and column.
     */
    fun addPiece(row: Int, col: Int, value: Char): Board {
        val value = value.lowercase()[0]
        require(row in 1..rows) {
            "Row must be between 1 and $rows"
        }
        require(col in 1..cols) {
            "Column must be between 1 and $cols"
        }
        require(value == 'b' || value == 'w') {
            "Value must be 'b' or 'w'"
        }
        if (this[row, col] == null) return this
        return this.copy(pieces = pieces + Piece(row, col, value))
    }

    /**
     * Converts a column character ('a', 'b', ...) to its corresponding integer index.
     */
    private fun charIndexToInt(col: Char): Int {
        val colLower = col.lowercase()[0]
        require(colLower in 'a'..'a' + cols - 1) {
            "Column must be between 'a' and '${'a' + cols - 1}'" }
        return colLower - 'a' + 1
    }

    /**
     * Starts the board with the initial pieces in the center.
     * @return A list of the initial pieces.
     */
    private fun startPieces(): Board {
        val midRow = rows / 2
        val midCol = cols / 2
        return this.copy(pieces = listOf(
            Piece(midRow, midCol, 'w'),
            Piece(midRow + 1, midCol + 1, 'w'),
            Piece(midRow, midCol + 1, 'b'),
            Piece(midRow + 1, midCol, 'b')
        ))
    }
}