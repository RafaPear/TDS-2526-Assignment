package pt.isel.reversi.core.game.data

/**
 * Standard result codes for game data access operations.
 *
 * Each enum entry captures a semantic category (success or specific failure family) and is used to
 * build a strongly typed [GDAResult] via the function call operator: e.g.
 *  val r = GDACodes.SUCCESS("Saved", true)
 *
 * Design guidelines:
 *  - Prefer returning a code + message instead of throwing for expected domain conditions
 *  - Reserve exceptions for truly exceptional / systemic faults (still mapped to IO_ERROR / UNKNOWN_ERROR)
 *  - Some codes (like DATA_NOT_FOUND) are not fatal and can be surfaced directly to a CLI / UI layer
 */
@Suppress("unused")
enum class GDACodes() {
    /** Operation completed without errors. */
    SUCCESS,
    /** Referenced entity or expected data was not found (missing file, empty file, absent headers). */
    DATA_NOT_FOUND,
    /** Low-level I/O error while reading or writing persistence medium. */
    IO_ERROR,
    /** Malformed or unexpected serialized representation (structure / tokens). */
    INVALID_FORMAT,
    /** Fallback code for uncategorized errors (defensive catch-all). */
    UNKNOWN_ERROR,
    /** Inconsistent state regarding available pieces header or its absence when required. */
    AVAILABLE_PIECES_ERROR,
    /** Inconsistent or invalid side (board size declaration). */
    SIDE_ERROR,
    /** Inconsistent or invalid board state (e.g. unparsable piece line when explicitly requested). */
    BOARD_ERROR;

    /**
     * Builds a [GDAResult] with this code.
     * @param message Optional detail about the outcome.
     * @param data Optional payload to return on success (or occasionally on error for context).
     */
    operator fun <T> invoke(message: String? = null, data: T? = null) =
        GDAResult(this, message, data)

    /** Named builder alternative to the invoke operator. */
    fun <T> buildDataAccessResult(message: String? = null, data: T? = null) =
        GDAResult(this, message, data)
}