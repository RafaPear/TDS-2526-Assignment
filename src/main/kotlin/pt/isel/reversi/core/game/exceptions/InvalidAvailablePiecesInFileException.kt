package pt.isel.reversi.core.game.exceptions

/**
 * Thrown when the persisted game file contains an invalid or missing `availablePieces:` header.
 *
 * Typical causes:
 * - The `availablePieces:` header is absent.
 * - The header uses an unexpected format (symbols not recognized).
 *
 * Consumers should treat this as a recoverable file-format error and present a clear
 * message to the user or offer to recreate the game file.
 *
 * @see pt.isel.reversi.core.game.localgda.GameFileAccess
 */
class InvalidAvailablePiecesInFileException(
    override val message: String = "The availablePieces in the file is invalid"
): Exception()
