package pt.isel.reversi.board

data class Coordinates(val row: Int, val col: Int) {
    constructor(row: Int, col: Char) : this(
        row,
        col.lowercase()[0] - 'a' + 1
    ) {
        require(col in 'a'..'z' || col in 'A'..'Z') {
            "Column must be a letter from a to z or A to Z"
        }
    }

    fun equals(other: Coordinates): Boolean =
        this.row == other.row && this.col == other.col

    operator fun plus(other: Coordinates): Coordinates =
        Coordinates(this.row + other.row, this.col + other.col)

    operator fun minus(other: Coordinates): Coordinates =
        Coordinates(this.row - other.row, this.col - other.col)

    fun isValid(boardSide: Int): Boolean =
        row in 1..boardSide && col in 1..boardSide
}