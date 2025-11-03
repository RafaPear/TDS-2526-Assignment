package pt.isel.reversi.cli.commands

import pt.isel.reversi.core.Game
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult

object TargetCmd : CommandImpl<Game>() {
    override val info: CommandInfo =
        CommandInfo(
            title = "Target Command",
            description = "Description for Target Command.",
            longDescription = "Long description for Target Command.",
            aliases = listOf("target", "tg"),
            usage = "target",
            minArgs = 0,
            maxArgs = 1
        )

    private val trueAlias = listOf("true", "yes", "1", "on")
    private val falseAlias = listOf("false", "no", "0", "off")

    override fun execute(
        vararg args: String,
        context: Game?
    ): CommandResult<Game> {
        if (context == null)
            return CommandResult.ERROR("No game context available.")


        val arg = args.getOrNull(0)?.lowercase()
        val newGame = when {
            trueAlias.contains(arg)  -> context.setTargetMode(true)

            falseAlias.contains(arg) -> context.setTargetMode(false)

            else                     -> context
        }

        return CommandResult.SUCCESS("Targeting is currently ${newGame.target}.", newGame)
    }
}