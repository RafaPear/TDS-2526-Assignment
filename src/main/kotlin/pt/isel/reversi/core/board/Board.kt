package pt.isel.reversi.core.board

/**
 * Represents a board game grid.
 *
 * @property pieces The list of pieces on the board.
 * @property side The size of the board (side x side).
 */
data class Board(
    val side: Int,
    private val pieces: List<Piece> = emptyList()
) : Iterable<Piece> {
    private val sideMin = 4
    private val sideMax = 26

    init {
        require(side in sideMin..sideMax) {
            "Side must be between $sideMin and $sideMax"
        }
        require(side % 2 == 0) {
            "Side must be even"
        }
    }

    fun Int.toCoordinates(): Coordinate {
        require(this in 0 until side * side) {
            "Index must be between 0 and ${side * side - 1}"
        }
        val row = (this / side) + 1
        val col = (this % side)
        return Coordinate(row, col)
    }

    /**
     * Checks if the specified row and column are within the bounds of the board.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    private fun checkPosition(coordinate: Coordinate) {
        require(coordinate.isValid(side)) {
            "Position ($coordinate is out of bounds)"
        }
    }

    /**
     * Gets the piece at the specified index like linear list.
     * @return The piece at the PieceType, or null if there is no piece.
     * @throws IllegalArgumentException if the index is out of bounds.
     */
    operator fun get(idx: Int): PieceType? {
        require(idx in 0 until side * side) {
            "Index must be between 0 and ${side * side - 1}"
        }
        val coordinate = idx.toCoordinates()
        return pieces.find { it.coordinate == coordinate }?.value
    }

    /**
     * Gets the piece at the specified row and column.
     * @return The piece at the specified position, or null if there is no piece.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    operator fun get(coordinate: Coordinate): PieceType? {
        checkPosition(coordinate)
        return pieces.find { it.coordinate == coordinate }?.value
    }

    /**
     * Changes the piece at the specified row and column from 'b' to 'w' or from 'w' to 'b'.
     * @return true if the piece was changed, false if there is no piece at the specified position.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun changePiece(coordinate: Coordinate): Board {
        checkPosition(coordinate)
        return changePieceNoCheks(coordinate)
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
        val coordinate = idx.toCoordinates()
        return changePieceNoCheks(coordinate)
    }

    private fun changePieceNoCheks(coordinate: Coordinate): Board {
        val value = this[coordinate]?.swap() ?: throw IllegalArgumentException("No piece at position $coordinate")
        return this.copy(pieces = pieces.map { piece ->
            if (piece.coordinate == coordinate)
                piece.copy(value = value)
            else
                piece
        })
    }

    /**
     * Adds a piece to the board at the specified row and column.
     * @throws IllegalArgumentException if the row or column are out of bounds.
     */
    fun addPiece(coordinate: Coordinate, value: PieceType): Board {
        checkPosition(coordinate)
        return addPieceNoCheks(coordinate, value)
    }

    private fun addPieceNoCheks(coordinate: Coordinate, value: PieceType): Board {
        if (this[coordinate] != null) throw IllegalArgumentException("There is already a piece at position $coordinate")
        return this.copy(pieces = pieces + Piece(coordinate, value))
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
        val coordinate = idx.toCoordinates()
        return addPieceNoCheks(coordinate, value)
    }

    /**
     * Starts the board with the initial pieces in the center.
     * @return A list of the initial pieces.
     */
    fun startPieces(): Board {
        val mid = side / 2
        return this.copy(
            pieces = listOf(
                Piece(Coordinate(mid, mid), PieceType.WHITE),
                Piece(Coordinate(mid + 1, mid + 1), PieceType.WHITE),
                Piece(Coordinate(mid, mid + 1), PieceType.BLACK),
                Piece(Coordinate(mid + 1, mid), PieceType.BLACK)
            )
        )
    }

    /**
     * Returns an iterator of the value of pieces on the board.
     */
    override fun iterator(): Iterator<Piece> = object : Iterator<Piece> {
        private val it = pieces.iterator()
        override fun hasNext() = it.hasNext()
        override fun next() = it.next()
    }
}