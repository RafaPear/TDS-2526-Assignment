package pt.isel.reversi

import pt.isel.reversi.cli.CLI
import pt.rafap.ktflag.style.Colors
import pt.rafap.ktflag.style.Colors.colorText

fun printUsage() {
    println("Usage: reversi [--cli] [--debug]")
    println("  --cli     : Run the command line interface version of the game.")
    println("  --debug   : Enable debug commands in the CLI.")
    println("  --app     : Run the GUI application version of the game. (Not implemented yet)")
}

fun main(args: Array<String>) {
    if (args.isEmpty())
        return printUsage()

    val debug = args.contains("--debug")
    val runCli = args.contains("--cli")
    val runApp = args.contains("--app")

    if (runCli && runApp) {
        println(colorText("[ERROR]: You must specify either --cli or --app", Colors.RED))
        return printUsage()
    }

    if (runCli || !runApp) {
        val cli = CLI().setDebug(debug)
        cli.startLoop()
    }
    else
        TODO("GUI application is not implemented yet")
}