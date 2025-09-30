package pt.isel.reversi

import pt.isel.reversi.commands.CmdNew
import pt.isel.reversi.parser.CommandParser
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

fun main(){
    var board = Board(8)

    val parser = CommandParser<Board>()

    parser.commandRegister.registerCommands(CmdNew)

    while (true){
        val result: CommandResult<Board>? = parser.readInputAndGetResult(board)

        if (result == null)
            println(colorText("[ERROR] Unknown command", Colors.RED))
        else if (result.isError)
            result.printError()
        else if (result.result != null)
            board = result.result as Board
    }
}