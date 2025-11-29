package pt.isel.reversi.utils

/**
 * Represents a configuration with key-value pairs.
 * @property map A map containing configuration key-value pairs.
 */
interface Config {
    val map: Map<String, String>
    fun getDefaultConfigFileEntries(): Map<String, String>
}