package pt.isel.reversi.utils

import java.io.File
import java.util.*

/**
 * Configuration loader that reads properties files and creates Config instances.
 * Automatically creates default configuration files if they don't exist.
 *
 * @param U The Config subclass type to instantiate with loaded values.
 * @property path File path to the configuration properties file.
 * @property factory Function to construct the Config instance from a key-value map.
 */
class ConfigLoader<U : Config>(
    val path: String,
    val factory: (Map<String, String>) -> U,
) {
    /**
     * Loads the configuration from the properties file.
     * Creates the file with default entries if it does not exist.
     * Synchronizes the file with the factory's default entries on load.
     *
     * @return The instantiated Config object with loaded properties.
     */
    fun loadConfig(): U {
        val props = Properties()
        val file = File(path)
        val defaultEntries = factory(emptyMap()).getDefaultConfigFileEntries()

        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            val entries = defaultEntries
            for (entry in entries)
                props.setProperty(entry.key, entry.value)

            file.outputStream().use { output ->
                props.store(output, "Configuration file created at ${file.absolutePath}")
            }
        }

        file.inputStream().use { input ->
            props.load(input)
        }

        val configMap = props.entries.associate { it.key.toString() to it.value.toString() }
        val factoryResult = factory(configMap)
        val newProps = Properties().also {
            it.putAll(factoryResult.getDefaultConfigFileEntries())
        }

        file.outputStream().use { output ->
            newProps.store(output, "Configuration file created at ${file.absolutePath}")
        }
        return factoryResult
    }
}