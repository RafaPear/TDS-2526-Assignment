package pt.isel.reversi.core.game.exceptions

/**
 * Thrown when an attempt is made to write a game snapshot over an existing persisted file
 * that is incompatible with the supplied game state.
 *
 * Example: the existing file declares a different board `side:` than the provided game's
 * `board.side`. In that case writing the new snapshot would corrupt the persisted data.
 *
 * Consumers should treat this as a precondition failure and surface a clear error to the user
 * (for example: request a fresh game file or confirm overwrite with side conversion).
 *
 * @see pt.isel.reversi.core.game.localgda.LocalGDA
 */
class InvalidGameWriteException(
    override val message: String = "The game write is invalid"
): Exception()
