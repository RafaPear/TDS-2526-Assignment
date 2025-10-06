package pt.isel.reversi.core.game.localgda

import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType

/**
 * Centralized message factory for [LocalGDA]. Keeps user-facing / log-facing strings in one place
 * to simplify localization or stylistic changes. Each function returns a concise description used
 * in [pt.isel.reversi.core.game.data.GDAResult.message].
 */
object LocalGDAMessages {
    /** File could not be resolved or is not writable. */
    fun fileNotFound(file: String) =
        "The file '$file' does not exist, is not a valid file, or does not have write permissions"

    /** File exists but contains zero lines. */
    fun fileEmpty(file: String) =
        "The file '$file' is empty — unable to load game data"

    fun fileReadSuccess(file: String) =
        "The file '$file' was successfully read"

    fun fileReadError(file: String, e: Exception) =
        "Error reading file '$file': ${e.message}"

    fun fileWriteError(file: String, e: Exception) =
        "Error writing to file '$file': ${e.message}"

    fun pieceSaved(file: String, piece: Piece) =
        "Piece '${piece.value.symbol}' saved at (${piece.coordinate.row}, ${piece.coordinate.col}) in file '$file'"

    fun gameWritten(file: String) =
        "Game successfully saved to file '$file'"

    fun passRecorded(file: String, type: PieceType) =
        "Player '${type.symbol}' passed the turn (recorded in '$file')"

    fun boardLoaded(file: String) =
        "Board successfully loaded from file '$file'"

    fun invalidSide(file: String) =
        "Invalid 'side' value found in '$file'. It must be an even number between 4 and 26"

    fun missingSide(file: String) =
        "Missing board size ('side') information in '$file'"

    fun invalidPiece() =
        "Invalid or malformed piece line in file"

    fun availablePiecesMissing(file: String) =
        "Missing 'availablePieces:' line in file '$file'"

    fun latestPieceFound(file: String, piece: Piece) =
        "Last piece found in file '$file': '${piece.value.symbol}' at (${piece.coordinate.row}, ${piece.coordinate.col})"

    fun noPiecesFound(file: String) =
        "No pieces found in file '$file'"
}
