package pt.isel.reversi.utils

import java.util.logging.ConsoleHandler
import java.util.logging.Logger

const val GAME_BASE_FOLDER = "data"

const val CONFIG_FOLDER = "$GAME_BASE_FOLDER/config"
const val CORE_CONFIG_FILE = "$CONFIG_FOLDER/reversi-core.properties"
const val CLI_CONFIG_FILE = "$CONFIG_FOLDER/reversi-cli.properties"
const val APP_CONFIG_FILE = "$CONFIG_FOLDER/reversi-app.properties"

val BASE_LOG_FILE_NAME = makePathString("logs/reversi-app")

val LOGGER: Logger = Logger.getGlobal().also {
    val consoleHandler = ConsoleHandler().also { handler ->
        handler.formatter = PlainFormatter()
    }
    it.addHandler(consoleHandler)
    it.useParentHandlers = false
}
