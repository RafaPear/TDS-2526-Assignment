package pt.isel.reversi


/**
 * Represents a board game grid.
 *
 * @property pieces The list of pieces on the board.
 * @property side The size of the board (side x side).
 */

@ConsistentCopyVisibility
data class Board private constructor(
    private val side: Int,
    private val pieces: List<Piece>
) : Iterable<Board.PieceType> {

    private val SIDE_MIN = 4
    private val SIDE_MAX = 26

    constructor(side: Int) : this(side = side, emptyList())

    init {
        require(side in SIDE_MIN..SIDE_MAX) {
            "Side must be between $SIDE_MIN and $SIDE_MAX"
        }
        require(side % 2 == 0) {
            "Side must be even"
        }
    }

    /**
     * Represents the type of piece on the board.
     */
    enum class PieceType(val symbol: Char) {
        BLACK('#'),
        WHITE('@');

        fun swap(): PieceType = if (this == BLACK) WHITE else BLACK
    }


    /**
     * Represents a piece on the board.
     */
    private data class Piece(val row: Int, val col: Int, val value: PieceType)


    fun Int.toCoordinates(): Pair<Int, Int> {
        require(this in 0 until side * side) {
            "Index must be between 0 and ${side * side - 1}"
        }
        val row = (this / side) + 1
        val col = (this % side)
        return Pair(row, col)
    }

    /**
     * Checks if the specified row and column are within the bounds of the board.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    private fun checkPosition(row: Int, col: Int) {
        require(row in 1..side) {
            "Row must be between 1 and $side"
        }
        require(col in 1..side) {
            "Column must be between 1 and $side"
        }
    }

    /**
     * Gets the piece at the specified row and column.
     * @return The piece at the specified position, or null if there is no piece.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    operator fun get(row: Int, col: Char): PieceType? = this[row, col.toIntIndex()]

    /**
     * Gets the piece at the specified index like linear list.
     * @return The piece at the PieceType, or null if there is no piece.
     * @throws IllegalArgumentException if the index is out of bounds.
     */
    operator fun get(idx: Int): PieceType? {
        require(idx in 0 until side * side) {
            "Index must be between 0 and ${side * side - 1}"
        }
        val (row, col) = idx.toCoordinates()
        return pieces.find { it.row == row && it.col == col }?.value
    }

    /**
     * Gets the piece at the specified row and column.
     * @return The piece at the specified position, or null if there is no piece.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    operator fun get(row: Int, col: Int): PieceType? {
        checkPosition(row, col)
        return pieces.find { it.row == row && it.col == col }?.value
    }


    /**
     * Changes the piece at the specified row and column from 'b' to 'w' or from 'w' to 'b'.
     * @return true if the piece was changed, false if there is no piece at the specified position.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun changePiece(row: Int, col: Char): Board = changePiece(row, col.toIntIndex())

    /**
     * Changes the piece at the specified row and column from 'b' to 'w' or from 'w' to 'b'.
     * @return true if the piece was changed, false if there is no piece at the specified position.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun changePiece(row: Int, col: Int): Board {
        checkPosition(row, col)
        return changePieceNoCheks(row, col)
    }

    /**
     * Changes the piece at the specified index like linear list from 'b' to 'w' or from 'w' to 'b'.
     * @param idx The index of the piece to change.
     * @return true if the piece was changed, false if there is no piece at the specified position.
     * @throws IllegalArgumentException if the index is out of bounds.
     */
    fun changePiece(idx: Int): Board {
        require(idx in 0 until side * side) {
            "Index must be between 0 and ${side * side - 1}"
        }
        val (row, col) = idx.toCoordinates()
        return changePieceNoCheks(row, col)
    }

    private fun changePieceNoCheks(row: Int, col: Int): Board {
        val value = this[row, col]?.swap() ?: throw IllegalArgumentException("No piece at position ($row, $col)")
        return this.copy(pieces = pieces.map { piece ->
            if (piece.row == row && piece.col == col)
                piece.copy(value = value)
            else
                piece
        })
    }

    /**
     * Adds a piece to the board at the specified row and column.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun addPiece(row: Int, col: Char, value: PieceType): Board =
        this.addPiece(row, col.toIntIndex(), value)


    /**
     * Adds a piece to the board at the specified row and column.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun addPiece(row: Int, col: Int, value: PieceType): Board {
        checkPosition(row, col)
        return addPieceNoCheks(row, col, value)
    }

    private fun addPieceNoCheks(row: Int, col: Int, value: PieceType): Board {
        if (this[row, col] != null) throw IllegalArgumentException("There is already a piece at position ($row, $col)")
        return this.copy(pieces = pieces + Piece(row, col, value))
    }

    /**
     * Adds a piece to the board at the specified index like linear list.
     * @param idx The index where the piece will be added.
     * @param value The type of piece to add.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun addPiece(idx: Int, value: PieceType): Board {
        require(idx in 0 until side * side) {
            "Index must be between 0 and ${side * side - 1}"
        }
        val (row, col) = idx.toCoordinates()
        return addPieceNoCheks(row, col, value)
    }

    /**
     * Converts a column character ('a', 'b', ...) to its corresponding integer index.
     */
    private fun Char.toIntIndex(): Int {
        val colLower = this.lowercase()[0]
        require(colLower in 'a'..'a' + side - 1) {
            "Column must be between 'a' and '${'a' + side - 1}'"
        }
        return colLower - 'a' + 1
    }

    /**
     * Starts the board with the initial pieces in the center.
     * @return A list of the initial pieces.
     */
    fun startPieces(): Board {
        val mid = side / 2
        return this.copy(
            pieces = listOf(
                Piece(row = mid, col = mid, value = PieceType.WHITE),
                Piece(row = mid + 1, col = mid + 1, value = PieceType.WHITE),
                Piece(row = mid, col = mid + 1, value = PieceType.BLACK),
                Piece(row = mid + 1, col = mid, value = PieceType.BLACK)
            )
        )
    }

    /**
     * Returns an iterator of the value of pieces on the board.
     */
    override fun iterator(): Iterator<PieceType> = object : Iterator<PieceType> {
        private val it = pieces.iterator()
        override fun hasNext() = it.hasNext()
        override fun next() = it.next().value
    }
}