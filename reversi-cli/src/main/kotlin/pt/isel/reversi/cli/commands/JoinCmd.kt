package pt.isel.reversi.cli.commands

import kotlinx.coroutines.runBlocking
import pt.isel.reversi.cli.pieceTypes
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.loadGame
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult

/**
 * Command to join a game.
 */
object JoinCmd : CommandImpl<Game>() {
    override val info: CommandInfo = CommandInfo(
        title = "Join",
        description = "Joins a game with the specified first player.",
        longDescription = "Joins a board with the specified first player. \n" + "If a name is provided, the board will be joined with that name.",
        aliases = listOf("j", "join"),
        usage = "join (<name>) [$pieceTypes]",
        minArgs = 1,
        maxArgs = 2
    )

    override fun execute(vararg args: String, context: Game?): CommandResult<Game> {
        if (context != null && context.currGameName != null) {
            runBlocking {
                context.saveEndGame()
            }
        }

        val name = args[0]
        val pTypeArg = args.getOrNull(1)?.firstOrNull()

        val pType = PieceType.fromSymbol(pTypeArg)

        if (pTypeArg != null && pType == null)
            return CommandResult.ERROR("Invalid piece type symbol provided: '$pTypeArg'.")

        val game = runBlocking { loadGame(name, pType) }

        println(ShowCmd.executeWrapper(context = game).message)

        return CommandResult.SUCCESS("Joined game Successfully", game)
    }
}