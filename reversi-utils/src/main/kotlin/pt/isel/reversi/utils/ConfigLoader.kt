package pt.isel.reversi.utils

import java.io.File
import java.util.*

/**
 * Configuration loader.
 * Loads configuration from a properties file and validates expected entries.
 */
class ConfigLoader<U: Config>(
    val path: String,
    val factory: (Map<String, String>) -> U,
) {
    /**
     * Loads the configuration into a Map.
     */
    fun loadConfig(): U {
        val props = Properties()
        val file = File(path)

        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            val entries = factory(emptyMap()).getDefaultConfigFileEntries()
            for (entry in entries)
                props.setProperty(entry.key, entry.value)
            props.store(file.outputStream(), "Configuration file created at ${file.absolutePath}")
        }

        props.load(file.inputStream())

        val configMap = props.entries.associate { it.key.toString() to it.value.toString() }

        return factory(configMap)
    }
}