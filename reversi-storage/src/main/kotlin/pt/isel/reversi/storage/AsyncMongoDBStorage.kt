package pt.isel.reversi.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [AsyncStorage] implementation via file + text strings.
 *
 * This method was based from [roby2014 - uni-projects/TDS](https://github.com/roby2014/uni-projects/tree/master/TDS)
 *
 * @param T Type of the domain entity
 * @param mongoDBConnection MongoDB connection information
 * @param databaseName Name of the MongoDB database
 * @param collectionName Name of the MongoDB collection
 */
data class AsyncMongoDBStorage<T>(
    private val mongoDBConnection: MongoDBConnection,
    private val databaseName: String,
    private val collectionName: String,
    override val serializer: Serializer<T, String>
) : AsyncStorage<String, T, String> {
    private val storage: MongoDBStorage<T> by lazy {
        MongoDBStorage(
            mongoDBConnection = mongoDBConnection,
            databaseName = databaseName,
            collectionName = collectionName,
            serializer = serializer
        )
    }

    override suspend fun new(id: String, factory: () -> T): T =
        withContext(Dispatchers.IO) { storage.new(id, factory) }

    override suspend fun load(id: String): T? = withContext(Dispatchers.IO) { storage.load(id) }

    override suspend fun save(id: String, obj: T) = withContext(Dispatchers.IO) { storage.save(id, obj) }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) { storage.delete(id) }

    override suspend fun lastModified(id: String): Long? = withContext(Dispatchers.IO) { storage.lastModified(id) }

    override suspend fun loadAllIds(): List<String> = withContext(Dispatchers.IO) { storage.loadAllIds() }

    override suspend fun close() = withContext(Dispatchers.IO) { storage.close() }
}