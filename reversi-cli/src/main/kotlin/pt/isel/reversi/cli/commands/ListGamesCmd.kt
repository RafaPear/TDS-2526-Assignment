package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.Game
import pt.isel.reversi.core.SAVES_FOLDER
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import java.io.File

object ListGamesCmd: CommandImpl<Game>() {
    override val info: CommandInfo =
        CommandInfo(
            title = "List Games",
            description = "Lists all available games.",
            longDescription = "Displays a list of all available games that can be joined or viewed.",
            aliases = listOf("listgames", "lg"),
            usage = "listgames",
            minArgs = 0,
            maxArgs = 0
        )

    override fun execute(
        vararg args: String,
        context: Game?
    ): CommandResult<Game> {
        val folder = File(SAVES_FOLDER)
        if (!folder.exists() || !folder.isDirectory) {
            return CommandResult.SUCCESS("No saved games found.", context)
        }

        val files = folder.listFiles()
        val gameNames = files?.map { it.nameWithoutExtension } ?: emptyList()
        val message = if (gameNames.isEmpty()) {
            "No saved games found."
        } else {
            "Available games:\n" + gameNames.joinToString(separator = "\n") { "- $it" }
        }
        return CommandResult.SUCCESS(message, context)
    }
}