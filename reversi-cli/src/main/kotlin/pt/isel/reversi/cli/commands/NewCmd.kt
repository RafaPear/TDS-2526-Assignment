package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.Environment
import pt.isel.reversi.core.Environment.First_Player_TURN
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.Player
import pt.isel.reversi.core.game.localgda.LocalGDA
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.cmd.CommandResult.ERROR

/**
 * Command to create a new game with the specified first player.
 */
object NewCmd : CommandImpl<GameImpl>() {

    private val pieceTypes = PieceType.entries.joinToString("|") { it.symbol.toString() }

    override val info: CommandInfo = CommandInfo(
        title = "New",
        description = "Creates a new game with the specified first player.",
        longDescription = "Creates a new board with the specified first player. \n" + "If a name is provided, the board will be saved with that name.",
        aliases = listOf("n", "new"),
        usage = "new ($pieceTypes) [<name>]",
        minArgs = 1,
        maxArgs = 2
    )

    override fun execute(vararg args: String, context: GameImpl?): CommandResult<GameImpl> {
        val firstPlayer = PieceType
                              .entries
                              .find { it.symbol.toString() == args[0] }
                          ?: return ERROR("First player must be one of: $pieceTypes")

        val name: String? = if (args.size == 2) args[1] else null

        val baseGame = Game(
            dataAccess = LocalGDA(),
            players = emptyList(),
            target = false,
            playerTurn = First_Player_TURN,
            board = Board(Environment.BOARD_SIDE),
            currGameName = null,
        )

        val game: GameImpl =
            if (name != null) {
                baseGame.copy(currGameName = name)
            } else {
                baseGame.copy(
                    players = baseGame.players + Player(firstPlayer.swap()),
                )
            }

        return CommandResult.SUCCESS("Game created Successfully", game)
    }
}