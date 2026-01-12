package pt.isel.reversi.core.storage

import pt.isel.reversi.core.CoreConfig
import pt.isel.reversi.core.storage.serializers.GameStateSerializer
import pt.isel.reversi.storage.AsyncFileStorage
import pt.isel.reversi.storage.AsyncMongoDBStorage
import pt.isel.reversi.storage.AsyncStorage
import pt.isel.reversi.storage.MongoDBConnection

/**
 * Enumeration of available storage types for game state persistence.
 * Each type provides a factory function to create the appropriate storage instance.
 *
 * @property storage Factory function that creates an AsyncStorage instance for the given folder.
 */
enum class GameStorageType(val storage: (StorageParams) -> AsyncStorage<String, GameState, String>) {
    /** File-based storage using serialization to disk. */
    FILE_STORAGE({ params ->
        require(params is StorageParams.FileStorageParams) { "Invalid parameters for FILE_STORAGE" }
        val folder = params.folder
        AsyncFileStorage(
            folder = folder,
            serializer = GameStateSerializer()
        )
    }),
    DATABASE_STORAGE({ params ->
        require(params is StorageParams.DatabaseStorageParams) { "Invalid parameters for DATABASE_STORAGE" }
        val mongoDBConnection = params.mongoDBConnection
        val databaseName = params.databaseName
        val collectionName = params.collectionName
        AsyncMongoDBStorage(
            mongoDBConnection = mongoDBConnection,
            databaseName = databaseName,
            collectionName = collectionName,
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
        fun fromConfigValue(value: String): GameStorageType {
            val result = entries.firstOrNull { it.name == value } ?: FILE_STORAGE
            return result
        }

        internal fun setUpStorage(config: CoreConfig): AsyncStorage<String, GameState, String> {
            return when (config.gameStorageType) {
                FILE_STORAGE -> setUpFileStorage(config)
                DATABASE_STORAGE -> setUpDatabaseStorage(config)
            }
        }

        internal fun setUpFileStorage(config: CoreConfig): AsyncStorage<String, GameState, String> {
            return FILE_STORAGE.storage(StorageParams.FileStorageParams(config.savesPath))
        }

        internal fun setUpDatabaseStorage(config: CoreConfig): AsyncStorage<String, GameState, String> {
            val mongoDBConnection = MongoDBConnection(
                host = config.dbURI,
                port = config.dbPort,
                user = config.dbUser,
                password = config.dbPassword
            )
            return DATABASE_STORAGE.storage(
                StorageParams.DatabaseStorageParams(
                    mongoDBConnection = mongoDBConnection,
                    databaseName = config.dbName,
                    collectionName = config.dbName
                )
            )
        }
    }
}