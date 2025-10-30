package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.game.GameImpl
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.cmd.CommandResult.ERROR

/**
 * Command to do a pass in a game.
 */
object PassCmd : CommandImpl<GameImpl>() {
    override val info: CommandInfo = CommandInfo(
        title = "Pass",
        description = "Passes the current player's turn.",
        longDescription = "Passes the current player's turn in the current game.",
        aliases = listOf("pass"),
        usage = "pass",
        minArgs = 0,
        maxArgs = 0
    )

    override fun execute(vararg args: String, context: GameImpl?): CommandResult<GameImpl> {
        if (context == null) {
            return ERROR("Game is not defined. Cannot join a game.")
        }

        try {
            context.pass()
            return CommandResult.SUCCESS(
                "Turn passed successfully",
                context
            )
        } catch (e: Exception) {
            return ERROR(e.message ?: "Error passing turn")
        }
    }
}