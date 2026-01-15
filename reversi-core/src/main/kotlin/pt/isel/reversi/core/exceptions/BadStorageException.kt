package pt.isel.reversi.core.exceptions

class BadStorageException(message: String) : ReversiException(message, ErrorType.CRITICAL)