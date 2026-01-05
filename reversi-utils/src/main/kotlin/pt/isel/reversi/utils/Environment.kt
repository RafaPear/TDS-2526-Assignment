package pt.isel.reversi.utils

import java.util.logging.ConsoleHandler
import java.util.logging.Logger

/** Base data directory for configuration and logs. */
const val BASE_FOLDER = "data"

/** Configuration directory path. */
const val CONFIG_FOLDER = "$BASE_FOLDER/config"
/** Core module configuration file path. */
const val CORE_CONFIG_FILE = "$CONFIG_FOLDER/reversi-core.properties"
/** CLI module configuration file path. */
const val CLI_CONFIG_FILE = "$CONFIG_FOLDER/reversi-cli.properties"
/** Desktop app module configuration file path. */
const val APP_CONFIG_FILE = "$CONFIG_FOLDER/reversi-app.properties"

/** Base path for application log files. */
val BASE_LOG_FILE_NAME = makePathString("logs/reversi-app")

/**
 * Global logger instance configured with console handler and plain formatter.
 * Used throughout the application for logging game events, errors, and diagnostics.
 */
val LOGGER: Logger = Logger.getGlobal().also {
    val consoleHandler = ConsoleHandler().also { handler ->
        handler.formatter = PlainFormatter()
    }
    it.addHandler(consoleHandler)
    it.useParentHandlers = false
}
