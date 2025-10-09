package pt.isel.reversi.core.game.exceptions

/**
 * Thrown when the persisted game file contains an invalid or missing `side:` header.
 *
 * Typical causes:
 * - The `side:` header is absent from the file.
 * - The header value is not a valid integer.
 *
 * This is a domain-level error signalling that the file cannot be used to
 * reconstruct a [pt.isel.reversi.core.board.Board].
 *
 * @see pt.isel.reversi.core.game.localgda.GameFileAccess
 */
class InvalidSideInFileException(
    override val message: String = "The side in the file is invalid"
): Exception()
