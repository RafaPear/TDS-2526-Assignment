package pt.isel.reversi.parser

import pt.rafap.ktflag.cmd.CommandRegister
import pt.rafap.ktflag.cmd.CommandResult
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

class CommandParser<T>(val prompt: String = colorText("> ", Colors.PURPLE)) {
    val commandRegister: CommandRegister<T> = CommandRegister()

    fun readInputAndGetResult(context: T?): CommandResult<T>? {
        print(prompt)
        val input = readln()
        if (input.isEmpty()) return null

        val parts = input.split(" ")
        val cmdName = parts[0]
        val args = parts.drop(1).toTypedArray()

        return commandRegister[cmdName]?.execute(*args, context = context)
    }
}