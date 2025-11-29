package pt.isel.reversi.cli.commands

import kotlinx.coroutines.runBlocking
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Coordinate
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.cmd.CommandResult.ERROR

/**
 * Command to do a play in a game.
 */
object PlayCmd : CommandImpl<Game>() {
    override val info: CommandInfo = CommandInfo(
        title = "Play",
        description = "Plays a move at the specified row and column.",
        longDescription = "Plays a move at the specified row and column in the current game. The coordinates can be provided as separate arguments or combined. For example, both 'play 3 4' and 'play 3A' are valid inputs.",
        aliases = listOf("p", "play"),
        usage = "play (row) (col) or play (rowcol)",
        minArgs = 1,
        maxArgs = 2
    )

    // 1 5
    // 1 A ou 1 a
    // 1A ou 1a
    // 15

    fun parseCoordinateArgs(args: List<String>): Coordinate? {

        val arg = args.joinToString("").trim()

        when {
            arg.length >= 2 -> {
                val colChar = arg.getOrNull(1) ?: return null
                val row = arg.getOrNull(0).toString().toIntOrNull() ?: return null

                if (colChar.isDigit()) {
                    val col = colChar.toString().toIntOrNull() ?: return null
                    return Coordinate(row, col)
                } else if (colChar.isLetter()) return Coordinate(row, colChar)
                else return null
            }

            else            -> return null
        }
    }

    override fun execute(vararg args: String, context: Game?): CommandResult<Game> {
        if (context == null) {
            return ERROR("Game is not defined. Cannot play.")
        }

        try {
            val coordinate = parseCoordinateArgs(args.toList()) ?: return ERROR("Invalid coordinates provided.")

            val game: Game = runBlocking { context.play(coordinate) }

            println(ShowCmd.executeWrapper(context = game).message)

            return CommandResult.SUCCESS(
                "Move played at (${coordinate.row}, ${coordinate.col})",
                game
            )
        } catch (e: Exception) {
            return ERROR("Error playing move: ${e.message}")
        }
    }
}