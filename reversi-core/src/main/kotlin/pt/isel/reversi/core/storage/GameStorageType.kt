package pt.isel.reversi.core.storage

import pt.isel.reversi.core.storage.serializers.GameStateSerializer
import pt.isel.reversi.storage.AsyncFileStorage
import pt.isel.reversi.storage.AsyncStorage

/**
 * Enumeration of available storage types for game state persistence.
 * Each type provides a factory function to create the appropriate storage instance.
 *
 * @property storage Factory function that creates an AsyncStorage instance for the given folder.
 */
enum class GameStorageType(val storage: (String) -> AsyncStorage<String, GameState, String>) {
    /** File-based storage using serialization to disk. */
    FILE_STORAGE({ folder ->
                     AsyncFileStorage(
                         folder = folder,
                         serializer = GameStateSerializer()
                     )
                 });

    companion object {
        /**
         * Retrieves a GameStorageType from its configuration value string.
         *
         * @param value The configuration string representing the storage type.
         * @return The matching GameStorageType, or FILE_STORAGE if not found.
         */
        fun fromConfigValue(value: String): GameStorageType =
            entries.firstOrNull { it.name == value } ?: FILE_STORAGE
    }
}