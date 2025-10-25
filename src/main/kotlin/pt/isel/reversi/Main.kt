package pt.isel.reversi

import pt.isel.reversi.cli.CLI
import pt.isel.reversi.cli.commands.ExitCmd
import pt.isel.reversi.cli.commands.JoinCmd
import pt.isel.reversi.cli.commands.NewCmd
import pt.isel.reversi.cli.commands.PassCmd
import pt.isel.reversi.cli.commands.PlayCmd
import pt.rafap.ktflag.cmd.args.CommandArg
import pt.rafap.ktflag.cmd.args.CommandArgsParser
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

fun printUsage() {
    println("Usage: reversi [--cli] [--debug]")
    println("  --cli     : Run the command line interface version of the game.")
    println("  --debug   : Enable debug commands in the CLI.")
    println("  --app     : Run the GUI application version of the game. (Not implemented yet)")
}

private val debugArg = CommandArg(
    name = "Debug",
    description = "Enables debug mode with additional commands.",
    aliases = arrayOf("--debug", "-d"),
    isRequired = false,
    returnsValue = false,
)

private val cliArg = CommandArg(
    name = "CLI",
    description = "Run the command line interface version of the game.",
    aliases = arrayOf("--cli", "-c"),
    isRequired = false,
    returnsValue = false,
)

private val appArg = CommandArg(
    name = "App",
    description = "Run the GUI application version of the game.",
    aliases = arrayOf("--app", "-a"),
    isRequired = false,
    returnsValue = false,
)

fun main(args: Array<String>) {
    val parser = CommandArgsParser(
        debugArg, cliArg, appArg
    )

    val parsed = parser.parseArgs(*args)

    val debug = parsed[debugArg] != null
    val runCli = parsed[cliArg] != null
    val runApp = parsed[appArg] != null

    if (runCli && runApp) {
        println(colorText("[ERROR]: You must specify either --cli or --app", Colors.RED))
        return printUsage()
    }

    if (runCli || !runApp) {
        val cli = CLI(
            arrayOf(
                PlayCmd,
                PassCmd,
                NewCmd,
                JoinCmd,
                ExitCmd
            ),
            debug
        )
        cli.startLoop()
    } else
        TODO("GUI application is not implemented yet")
}