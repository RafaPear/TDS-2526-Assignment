package pt.isel.reversi.core.storage

import pt.isel.reversi.storage.MongoDBConnection

sealed class StorageParams {
    data class FileStorageParams(val folder: String) : StorageParams()
    data class DatabaseStorageParams(
        val mongoDBConnection: MongoDBConnection, val databaseName: String, val collectionName: String
    ) : StorageParams()
}