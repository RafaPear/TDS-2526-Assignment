package pt.isel.reversi.app.exceptions

import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException

class NoPieceSelected(
    message: String = "No piece selected",
    type: ErrorType = ErrorType.INFO,
) : ReversiException(message, type)