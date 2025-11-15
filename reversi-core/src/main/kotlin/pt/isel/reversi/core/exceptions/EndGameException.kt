package pt.isel.reversi.core.exceptions

class EndGameException(
    message: String = "The game has ended",
    type: ErrorType
) : ReversiException(message, type)