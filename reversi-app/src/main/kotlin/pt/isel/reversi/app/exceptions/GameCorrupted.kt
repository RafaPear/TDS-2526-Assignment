package pt.isel.reversi.app.exceptions

import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException

class GameCorrupted(
    message: String = "The game data is corrupted and cannot be loaded.",
    type: ErrorType = ErrorType.ERROR
): ReversiException(message, type)