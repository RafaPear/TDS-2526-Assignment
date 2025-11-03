package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.Game
import pt.isel.reversi.core.stringifyBoard
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult

/**
 * ShowCmd prints the current game state: board layout and player scores.
 * It expects a `Game` context and returns an error when the game or board is not initialized.
 */
object ShowCmd : CommandImpl<Game>() {
    override val info: CommandInfo = CommandInfo(
        title = "Show",
        description = "Shows the current game state.",
        longDescription = "Displays the current board, player scores, and other relevant game information.",
        aliases = listOf("s", "show"),
        usage = "show",
        minArgs = 0,
        maxArgs = 0
    )

    override fun execute(
        vararg args: String,
        context: Game?
    ): CommandResult<Game> {
        if (context == null)
            return CommandResult.ERROR("Game is not defined. Cannot show game state.")

        val gs = context.gameState ?: return CommandResult.ERROR("Game is not initialized.")

        val players = gs.players
        val lastPLayer = gs.lastPlayer
        val name = context.currGameName
        val myPiece = players.firstOrNull()?.type?.symbol

        val builder = StringBuilder()
        if (myPiece != null && name != null) {
            builder.appendLine("You are playing as '$myPiece' at game '$name'")
            builder.appendLine("-------------------------")
        }
        builder.appendLine("Current Game State:")
        builder.appendLine(context.stringifyBoard())
        builder.appendLine("Player Scores:")
        players.forEach { player ->
            builder.appendLine("Player ${player.type.symbol}: ${player.points} points")
        }
        builder.appendLine("Player turn: ${lastPLayer.swap().symbol}")

        return CommandResult.SUCCESS(builder.toString(), context)
    }

}