package pt.isel.reversi.core.exceptions

/**
 * Enum representing different types of error levels for exceptions in the Reversi game.
 */
enum class ErrorType(val level: String) {
    INFO("INFO"),
    WARNING("WARNING"),
    ERROR("ERROR"),
    CRITICAL("CRITICAL")
}