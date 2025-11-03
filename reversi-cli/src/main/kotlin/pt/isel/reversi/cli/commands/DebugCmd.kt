package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.Game
import pt.isel.reversi.core.stringifyBoard
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult

/**
 * Command to debug the game state.
 */
object DebugCmd: CommandImpl<Game>() {
    override val info: CommandInfo = CommandInfo(
        title = "Debug",
        description = "Debug command for testing purposes.",
        longDescription = "A command used for debugging the application. This command is intended for development and testing purposes only.",
        aliases = listOf("dbg", "debug"),
        usage = "debug",
        minArgs = 0,
        maxArgs = 0
    )

    override fun execute(
        vararg args: String,
        context: Game?
    ): CommandResult<Game> {

        if (context == null) return CommandResult.SUCCESS("Game context is null")

        val target  = context.target
        val name    = context.currGameName
        val state   = context.gameState
        val count   = context.countPass

        val sb = StringBuilder()

        sb.appendLine("Debug Information:")
        sb.append("Game State: ")
        if (state == null) {
            sb.appendLine("Not initialized")
        } else {
            sb.appendLine("Initialized")
            sb.appendLine("Last play was from ${state.lastPlayer.symbol}")
            sb.appendLine("Players:")
            state.players.forEach { player ->
                sb.appendLine(" - Player ${player.type.symbol}: ${player.points} points")
            }
            val board = state.board
            sb.appendLine("Board: ")
            sb.appendLine(" - Side: ${board.side}")
            sb.appendLine(" - Total of Pieces on board: ${board.count()}")
            sb.appendLine(" - Board Representation:")
            sb.appendLine(context.stringifyBoard())
            sb.appendLine("Target: $target")
            sb.appendLine("Current Game Name: $name")
            sb.appendLine("Consecutive Passes: $count")
        }

        return CommandResult.SUCCESS(sb.toString(), context)
    }
}