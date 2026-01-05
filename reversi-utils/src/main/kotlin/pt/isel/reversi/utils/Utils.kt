package pt.isel.reversi.utils

import java.io.File
import java.time.LocalDate
import java.net.URI
import java.net.URL
import java.nio.file.*
import java.util.stream.Collectors
/**
 * Makes a path string by joining the given parts with the base game folder.
 * @param parts The parts of the path to join.
 * @return The joined path string.
 */
fun makePathString(vararg parts: String): String =
    listOf(BASE_FOLDER, *parts).joinToString(separator = "/")

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
 * Generic function to load resources from a folder and transform them.
 * Handles both IDE and JAR execution transparently.
 *
 * @param folder The resource folder path (e.g., "audios/", "images/")
 * @param transform Function to transform each resource (fileName, URL) -> T?
 * @return List of successfully transformed resources
 */
fun <T> loadResourcesFromFolder(
    folder: String,
    transform: (fileName: String, url: URL) -> T?
): List<T> {
    return try {
        val files = listResourceFiles(folder)
        LOGGER.info("Loading resources from $folder")
        LOGGER.info("Found ${files.size} files in $folder")

        files.mapNotNull { fileName ->
            val resourcePath = "$folder$fileName"
            try {
                val url = Thread.currentThread()
                    .contextClassLoader
                    .getResource(resourcePath)
                    ?: throw IllegalArgumentException("Resource '$resourcePath' not found")

                transform(fileName, url)
            } catch (e: Exception) {
                LOGGER.warning("Failed to process $fileName: ${e.message}")
                null
            }
        }
    } catch (e: Exception) {
        LOGGER.warning("Could not load resources from $folder: ${e.message}")
        emptyList()
    }
}

/**
 * Lists all files in a resource directory, works both in IDE and JAR.
 * @param path The path to the resource directory.
 * @return List of file names in the directory.
 */
private fun listResourceFiles(path: String): List<String> {
    val normalizedPath = path.removeSuffix("/")
    val resource = Thread.currentThread()
        .contextClassLoader
        .getResource(normalizedPath)
        ?: throw IllegalArgumentException("Resource directory '$normalizedPath' not found")

    return when (resource.protocol) {
        "file" -> {
            // Running from IDE or exploded directory
            File(resource.toURI()).listFiles()?.map { it.name } ?: emptyList()
        }
        "jar" -> {
            // Running from JAR
            val jarPath = resource.toString()
                .substringAfter("jar:file:")
                .substringBefore("!")

            FileSystems.newFileSystem(URI.create("jar:file:$jarPath"), emptyMap<String, Any>()).use { fs ->
                val folderPath = fs.getPath(normalizedPath)
                Files.walk(folderPath, 1)
                    .filter { Files.isRegularFile(it) }
                    .map { it.fileName.toString() }
                    .collect(Collectors.toList())
            }
        }
        else -> throw IllegalArgumentException("Unsupported protocol: ${resource.protocol}")
    }
}

/**
 * Loads a resource file from the classpath.
 * Only use this if you really need a File object. For most cases, use getResource() directly.
 * @param path The path to the resource file.
 * @return The resource file (as temporary file if running from JAR).
 */
@Suppress("Unused")
fun loadResource(path: String): File {
    val input = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(path)
        ?: throw IllegalArgumentException("Resource '$path' not found")

    val temp = kotlin.io.path.createTempFile(
        prefix = "kpb-resource-",
        suffix = "-" + path.substringAfterLast('/')
    ).toFile()

    temp.outputStream().use { out ->
        input.copyTo(out)
    }

    temp.deleteOnExit()
    return temp
}