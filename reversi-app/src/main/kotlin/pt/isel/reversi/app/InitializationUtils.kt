package pt.isel.reversi.app

import pt.isel.reversi.app.gameAudio.loadGameAudioPool
import pt.isel.reversi.utils.audio.AudioPool
import pt.isel.reversi.utils.setLoggerFilePath
import pt.rafap.ktflag.cmd.args.CommandArg
import pt.rafap.ktflag.cmd.args.CommandArgsParser

/**
 * Container for application initialization results.
 *
 * @property audioPool The initialized audio pool for sound effects and music.
 */
data class InitializedArgs(
    val audioPool: AudioPool
)

/** Command-line argument enabling file-based logging. */
val logArg = CommandArg(
    name = "log",
    aliases = arrayOf("-l"),
    description = "If set, enables logging to a file named reversi-app.log",
    returnsValue = false,
    isRequired = false
)

/** Command-line argument disabling audio playback. */
val noAudioArg = CommandArg(
    name = "no-audio",
    aliases = arrayOf("-na"),
    description = "If set, disables audio playback",
    returnsValue = false,
    isRequired = false
)

/** Command-line argument displaying help information. */
val helpArg = CommandArg(
    name = "help",
    aliases = arrayOf("-h", "--help"),
    description = "Displays help information about the command-line arguments",
    returnsValue = false,
    isRequired = false
)

/** Array of all supported command-line arguments. */
val allArgs = arrayOf(
    logArg, noAudioArg, helpArg
)

/** Parser for command-line arguments. */
val argsParser = CommandArgsParser(*allArgs)

/**
 * Initializes application arguments including logging and audio settings.
 * Parses command-line arguments and configures the application accordingly.
 *
 * @param args Command-line arguments array.
 * @return InitializedArgs with configured audio pool, or null if help was requested.
 */
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
    val loadedPool = loadGameAudioPool(AppThemes.DARK.appTheme) {}
    val audioPool = if (audioEnabled) loadedPool else loadedPool.also { it.mute(true) }
    return InitializedArgs(audioPool)
}