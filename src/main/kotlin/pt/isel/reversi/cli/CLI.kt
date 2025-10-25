package pt.isel.reversi.cli

import pt.isel.reversi.core.Environment
import pt.isel.reversi.core.Environment.firstPlayerTurn
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.localgda.LocalGDA
import pt.rafap.ktflag.CommandParser
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandResultType
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

class CLI(
    val commands: Array<CommandImpl<GameImpl>>,
    val debug: Boolean = false,
    val welcomeMessage: String = "Welcome to Reversi CLI!",
    val debugCommands: Array<CommandImpl<GameImpl>> = arrayOf(),
) {
    private fun logDebug(message: String) {
        if (debug) println(colorText("[DEBUG] $message", Colors.YELLOW))
    }

    /**
     * Entry point for the CLI version of the Reversi game.
     * Initializes the board and command parser, and handles user input.
     */
    fun startLoop() {
        /**
         * The current game board. Initialized with size 8.
         */
        var game: GameImpl = Game(
            dataAccess = LocalGDA(),
            players = emptyList(),
            target = false,
            playerTurn = firstPlayerTurn,
            board = Board(Environment.BOARD_SIDE),
            currGameName = null,
        )

        val commands = commands + debugCommands

        val parser = CommandParser(*commands)

        println(colorText(welcomeMessage, Colors.INFO_COLOR))
        while (true) {
            val input = parser.readInput()
            val result = parser.parseInputToResult(input, game)

            if (result == null) {
                println(colorText("[ERROR] Unknown command", Colors.RED))
                continue
            }

            when {
                result.type == CommandResultType.UNKNOWN_COMMAND  -> parser.printUnknownCommandError(input, result)

                result.type != CommandResultType.SUCCESS          -> result.printError()

                result.type == CommandResultType.SUCCESS && debug -> logDebug(result.message)

                result.result != null                             -> game = result.result!!
            }
        }
    }
}
