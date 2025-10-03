package pt.isel.reversi.commands

import pt.isel.reversi.board.Board
import pt.isel.reversi.board.PieceType
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.cmd.CommandResult.*
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

object NewCmd: CommandImpl<Board>() {
    val pieceTypes = PieceType.entries.joinToString("|") { it.symbol.toString() }

    override val info: CommandInfo =
        CommandInfo(
            title = "New",
            description = "Creates a new game with the specified first player.",
            longDescription =
            "Creates a new board with the specified first player. \n" +
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
        val firstPlayer = PieceType.entries.find { it.symbol.toString() == arg[0] }
                          ?: return ERROR("First player must be one of: $pieceTypes")

        println(colorText("First player: $firstPlayer", Colors.INFO_COLOR))

        return NOT_IMPLEMENTED("New game command is not implemented yet")
    }
}