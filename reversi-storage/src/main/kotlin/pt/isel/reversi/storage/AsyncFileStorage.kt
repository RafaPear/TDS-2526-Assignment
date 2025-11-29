package pt.isel.reversi.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [AsyncStorage] implementation via file + text strings.
 *
 * This method was based from [roby2014 - uni-projects/TDS](https://github.com/roby2014/uni-projects/tree/master/TDS)
 *
 * @param T Type of the domain entity
 * @param folder Folder where the file will be stored
 */
data class AsyncFileStorage<T>(
    private val folder: String,
    override val serializer: Serializer<T, String>
) : AsyncStorage<String, T, String> {
    private val fileStorage = FileStorage(folder, serializer)

    override suspend fun new(id: String, factory: () -> T): T = withContext(Dispatchers.IO) { fileStorage.new(id, factory) }

    override suspend fun load(id: String): T? = withContext(Dispatchers.IO) { fileStorage.load(id) }

    override suspend fun save(id: String, obj: T) = withContext(Dispatchers.IO) { fileStorage.save(id, obj) }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) { fileStorage.delete(id) }

    override suspend fun lastModified(id: String): Long? = withContext(Dispatchers.IO) { fileStorage.lastModified(id) }
}