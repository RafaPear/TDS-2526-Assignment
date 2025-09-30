package pt.isel.reversi.commands

import pt.isel.reversi.Board
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.cmd.CommandResult.NOT_IMPLEMENTED
import pt.rafap.ktflag.cmd.CommandResult.INVALID_ARGS
import pt.rafap.ktflag.cmd.CommandUtils.verifyArgsCount

object CmdNew: CommandImpl<Board> {
    val pieceTypes = Board.PieceType.entries.joinToString("|") { it.symbol.toString() }

    override val info: CommandInfo =
        CommandInfo(
            title = "New",
            description = "Creates a new board with the specified number of rows and columns.",
            longDescription =
            "Creates a new board with the specified first player. " +
                    "If a name is provided, the board will be saved with that name.",
            aliases = listOf("n", "new"),
            usage = "new ($pieceTypes) [<name>]",
            minArgs = 1,
            maxArgs = 2
        )

    override fun execute(
        vararg arg: String,
        context: Board?
    ): CommandResult<Board> {
        if (!this.verifyArgsCount(arg.size)) return INVALID_ARGS(info, arg.size)
        return NOT_IMPLEMENTED("New game command is not implemented yet")
    }
}