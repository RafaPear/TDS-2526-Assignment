package pt.isel.reversi.cli

import pt.isel.reversi.cli.commands.ExitCmd
import pt.isel.reversi.cli.commands.NewCmd
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.MockGame
import pt.isel.reversi.core.game.localgda.LocalGDA
import pt.rafap.ktflag.CommandParser
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandResultType
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

class CLI private constructor(
    private val debug: Boolean = false,
    private val extraCommands: Array<CommandImpl<GameImpl>> = emptyArray(),
    private val welcomeMessage: String = "Welcome to Reversi CLI!"
) {
    constructor() : this(false)

    private fun copy(
        debug: Boolean = this.debug,
        extraCommands: Array<CommandImpl<GameImpl>> = this.extraCommands,
        welcomeMessage: String = this.welcomeMessage
    ) = CLI(debug, extraCommands, welcomeMessage)

    fun setDebug(): CLI {
        return setDebug(true)
    }

    fun setDebug(state: Boolean): CLI {
        return this.copy(debug = state)
    }

    fun addExtraCommands(vararg commands: CommandImpl<GameImpl>): CLI {
        return this.copy(extraCommands = this.extraCommands + commands)
    }

    fun setWelcomeMessage(message: String): CLI {
        return this.copy(welcomeMessage = message)
    }

    private fun logDebug(message: String) {
        if (debug)
            println(colorText("[DEBUG] $message", Colors.YELLOW))
    }

    /**
     * Entry point for the CLI version of the Reversi game.
     * Initializes the board and command parser, and handles user input.
     */
    fun startLoop() {
        logDebug("Debug mode is ON")

        /**
         * The current game board. Initialized with size 8.
         */
        var game: GameImpl = MockGame.OnePlayer(LocalGDA(), "game.txt")
        logDebug("Instantiated Game")

        val debugCommands: Array<CommandImpl<GameImpl>> = if (debug) arrayOf(
            // Add debug commands here
        ) else arrayOf()

        val commands = arrayOf(NewCmd, ExitCmd) + debugCommands + extraCommands

        val parser = CommandParser(*commands)
        parser.getAllCommands().forEach { logDebug("Loaded command: ${it.info.title}") }
        logDebug("Command parser initialized with ${parser.getAllCommands().size} commands")

        println(colorText(welcomeMessage, Colors.INFO_COLOR))
        while (true) {
            val input = parser.readInput()
            val result = parser.parseInputToResult(input, game)

            if (result == null) {
                println(
                    colorText(
                        "[ERROR] Unknown command",
                        Colors.RED
                    )
                )
                continue
            }

            when {
                result.type == CommandResultType.UNKNOWN_COMMAND ->
                    parser.printUnknownCommandError(input, result)

                result.type != CommandResultType.SUCCESS         ->
                    result.printError()

                result.result != null                            ->
                    game = result.result!!
            }
        }
    }
}