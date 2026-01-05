package pt.isel.reversi.app.exceptions

import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException

/**
 * Exception thrown when a required text input field is empty.
 *
 * @param message Descriptive error message.
 * @param type The error severity type (default: INFO).
 */
class TextBoxIsEmpty(
    message: String = "Text box is empty",
    type: ErrorType = ErrorType.INFO
) : ReversiException(message, type)