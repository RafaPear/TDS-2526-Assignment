package pt.isel.reversi.storage

import okio.FileSystem
import okio.Path.Companion.toPath
import pt.isel.reversi.utils.TRACKER

/**
 * [Storage] implementation via file + text strings.
 *
 * This method was based from [roby2014 - uni-projects/TDS](https://github.com/roby2014/uni-projects/tree/master/TDS)
 *
 * @param T Type of the domain entity
 * @param folder Folder where the file will be stored
 */
data class FileStorage<T>(
    private val folder: String,
    override val serializer: Serializer<T, String>
) : Storage<String, T, String> {

    init {
        TRACKER.trackClassCreated(this)
    }

    /** Storage file path for entity identified by [id]. */
    private fun path(id: String) = "$folder/$id.txt".toPath()

    override fun new(id: String, factory: () -> T): T {
        val fs = FileSystem.SYSTEM

        require(!fs.exists(path(id))) { "There is already an entity with given id '$id'" }

        val obj = factory()
        val objStr = serializer.serialize(obj)

        path(id).parent?.let { fs.createDirectories(it) }

        fs.write(path(id), true) {
            writeUtf8(objStr)
        }

        return obj
    }

    override fun load(id: String): T? {
        val fs = FileSystem.SYSTEM

        if (!fs.exists(path(id))) return null

        // check if file is not in use by another process
        fs.metadata(path(id))
        val content = fs.read(path(id)) { readUtf8() }

        return serializer.deserialize(content)
    }

    override fun save(id: String, obj: T) {
        val fs = FileSystem.SYSTEM

        require(fs.exists(path(id))) { "There is no entity with given id '$id'" }

        val objStr = serializer.serialize(obj)

        fs.write(path(id), false) {
            writeUtf8(objStr)
        }
    }

    override fun delete(id: String) {
        val fs = FileSystem.SYSTEM

        require(fs.exists(path(id))) { "There is no entity with given id '$id'" }

        fs.delete(path(id))
    }

    override fun lastModified(id: String): Long? {
        val fs = FileSystem.SYSTEM
        if (!fs.exists(path(id))) return null

        return fs.metadata(path(id)).lastModifiedAtMillis
    }

    override fun loadAllIds(): List<String> {
        val fs = FileSystem.SYSTEM
        val dirPath = folder.toPath()

        if (!fs.exists(dirPath)) return emptyList()

        return fs.list(dirPath)
            .filter { fs.metadata(it).isRegularFile }
            .map { it.name.removeSuffix(".txt") }
    }

    override fun close() {
        // No resources to release
    }
}