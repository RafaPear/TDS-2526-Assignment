package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.Player
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.cmd.CommandResult.ERROR

/**
 * Command to join a game.
 */
object JoinCmd : CommandImpl<GameImpl>() {
    override val info: CommandInfo = CommandInfo(
        title = "Join",
        description = "Joins a game with the specified first player.",
        longDescription = "Joins a board with the specified first player. \n" + "If a name is provided, the board will be joined with that name.",
        aliases = listOf("j", "join"),
        usage = "join (<name>)",
        minArgs = 1,
        maxArgs = 1
    )

    override fun execute(vararg args: String, context: GameImpl?): CommandResult<GameImpl> {
        val name: String = args[0]
        if (context == null) {
            return ERROR("Game is not defined. Cannot join a game.")
        }

        try {
            var game: GameImpl = context.copy(
                currGameName = name,
            )

            val pieces = game.pieceOptions()

            if (pieces.isEmpty())
                return ERROR("No available pieces to join the game.")

            val piece: PieceType =
                if (pieces.size > 1) {
                    println("Available pieces to join the game: ${pieces.joinToString(", ") { it.symbol.toString() }}")
                    print("Choose your piece: ")
                    val input = readlnOrNull()?.trim()
                    val selectedPiece = pieces.find { it.symbol.toString() == input }
                    if (selectedPiece == null) {
                        return ERROR("Invalid piece selected. Please choose one of: ${pieces.joinToString(", ") { it.symbol.toString() }}")
                    }
                    selectedPiece
                } else pieces.first()

            game = game.copy(players = listOf(Player(piece))).refresh()

            return CommandResult.SUCCESS("Game created Successfully", game)
        } catch (e: Exception) {
            return ERROR("Error joining game: ${e.message}")
        }
    }
}