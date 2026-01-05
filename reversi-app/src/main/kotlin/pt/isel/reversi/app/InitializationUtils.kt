package pt.isel.reversi.app

import pt.isel.reversi.app.gameAudio.loadGameAudioPool
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioPool
import pt.isel.reversi.utils.setLoggerFilePath
import pt.rafap.ktflag.cmd.args.CommandArg
import pt.rafap.ktflag.cmd.args.CommandArgsParser

data class InitializedArgs(
    val audioPool: AudioPool
)

val logArg = CommandArg(
    name = "log",
    aliases = arrayOf("-l"),
    description = "If set, enables logging to a file named reversi-app.log",
    returnsValue = false,
    isRequired = false
)

val noAudioArg = CommandArg(
    name = "no-audio",
    aliases = arrayOf("-na"),
    description = "If set, disables audio playback",
    returnsValue = false,
    isRequired = false
)

val helpArg = CommandArg(
    name = "help",
    aliases = arrayOf("-h", "--help"),
    description = "Displays help information about the command-line arguments",
    returnsValue = false,
    isRequired = false
)

val allArgs = arrayOf(
    logArg, noAudioArg, helpArg
)

val argsParser = CommandArgsParser(*allArgs)

fun initializeAppArgs(args: Array<String>): InitializedArgs? {
    val parsedArgs = argsParser.parseArgs(*args)

    val help = parsedArgs[helpArg]
    if (help != null) {
        allArgs.forEach {
            println("${it.name} (${it.aliases.joinToString(", ")}): ${it.description}")
        }
        return null
    }

    val logToFileName = parsedArgs[logArg]
    if (logToFileName != null) setLoggerFilePath()

    val audioEnabled = parsedArgs[noAudioArg] == null
    LOGGER.info("Audio enabled: $audioEnabled")
    val audioPool = if (audioEnabled) loadGameAudioPool() else AudioPool(emptyList())
    return InitializedArgs(audioPool)
}