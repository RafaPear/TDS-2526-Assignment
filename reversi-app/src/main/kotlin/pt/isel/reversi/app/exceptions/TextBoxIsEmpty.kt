package pt.isel.reversi.app.exceptions

import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException

class TextBoxIsEmpty(
    message: String = "Text box is empty",
    type: ErrorType = ErrorType.INFO
) : ReversiException(message, type)