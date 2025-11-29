package pt.isel.reversi.utils

import java.io.File
import java.time.LocalDate

/**
 * Makes a path string by joining the given parts with the base game folder.
 * @param parts The parts of the path to join.
 * @return The joined path string.
 */
fun makePathString(vararg parts: String): String =
    listOf(GAME_BASE_FOLDER, *parts).joinToString(separator = "/")

/**
 * Sets the logger to log to a file with a name based on the current date.
 * If a file with the same name already exists, a counter is added to the name.
 */
fun setLoggerFilePath() {
    val date = LocalDate.now()
    var name = "${BASE_LOG_FILE_NAME}-$date.log"
    var count = 1
    while (File(name).exists()) {
        name = "${BASE_LOG_FILE_NAME}-$date-${count}.log"
        count++
    }
    File(name).parentFile?.mkdirs()
    File(name).createNewFile()
    val logFileHandler = java.util.logging.FileHandler(name, true).also {
        it.formatter = PlainFormatter()
    }
    LOGGER.addHandler(logFileHandler)
    LOGGER.info("Logging to file '$name' enabled.")
}

/**
 * Loads a resource file from the classpath.
 * @param path The path to the resource file.
 * @return The resource file.
 * @throws IllegalArgumentException if the resource file is not found.
 */
fun loadResource(path: String): File {
    val classloader = Thread.currentThread().getContextClassLoader()
    val resource = classloader.getResource(path)
                   ?: throw IllegalArgumentException("Resource '$path' not found")
    return File(resource.toURI())
}