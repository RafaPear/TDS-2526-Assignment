package pt.isel.reversi.storage

import org.junit.Before
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFails

class FileStorageTest {
    data class MockData(val id: Int, val name: String)

    class TestSerializer : Serializer<MockData, String> {
        override fun serialize(obj: MockData): String {
            return "${obj.id},${obj.name}"
        }

        override fun deserialize(obj: String): MockData {
            val parts = obj.split(",")
            return MockData(parts[0].toInt(), parts[1])
        }
    }

    val fileStorage = FileStorage(
        folder = "test-saves",
        serializer = TestSerializer()
    )

    @Before
    fun cleanup() {
        File("test-saves").deleteRecursively()
    }

    @Test
    fun `Run new at an already existing id fails`() {

        assertFails {
            fileStorage.new(1.toString()) {
                MockData(1, "Test1")
            }
            fileStorage.new(1.toString()) {
                MockData(1, "Test1")
            }
        }
    }

    @Test
    fun `Run save at a non existing id fails`() {

        assertFails {
            fileStorage.save(1.toString(), MockData(1, "Test1"))
        }
    }

    @Test
    fun `Run load at a non existing id returns null`() {

        val data = fileStorage.load(1.toString())
        assert(data == null)
    }

    @Test
    fun `Run new and load works`() {

        val data1 = fileStorage.new(1.toString()) { MockData(1, "Test1") }
        val data2 = fileStorage.load(1.toString())

        assert(data1 == data2)
    }

    @Test
    fun `Run new, save and load works`() {

        fileStorage.new(1.toString()) {
            MockData(1, "Test1")
        }
        val updatedData = MockData(1, "UpdatedTest1")
        fileStorage.save(1.toString(), updatedData)
        val data2 = fileStorage.load(1.toString())

        assert(data2 == updatedData)
    }

    @Test
    fun `Run new and delete works`() {

        fileStorage.new(1.toString()) { MockData(1, "Test1") }
        fileStorage.delete(1.toString())
        val data = fileStorage.load(1.toString())

        assert(data == null)
    }

    @Test
    fun `Run delete at a non existing id fails`() {

        assertFails {
            fileStorage.delete(1.toString())
        }
    }

    @Test
    fun `Run multiple operations`() {

        val data1 = fileStorage.new(1.toString()) { MockData(1, "Test1") }
        val data2 = fileStorage.load(1.toString())
        assert(data1 == data2)

        val updatedData = MockData(1, "UpdatedTest1")
        fileStorage.save(1.toString(), updatedData)
        val data3 = fileStorage.load(1.toString())
        assert(data3 == updatedData)

        fileStorage.delete(1.toString())
        val data4 = fileStorage.load(1.toString())
        assert(data4 == null)
    }

    @Test
    fun `Test lastModified returns correct timestamp`() {

        val beforeCreation = System.currentTimeMillis()
        fileStorage.new(1.toString()) { MockData(1, "Test1") }
        val afterCreation = System.currentTimeMillis()

        val lastModified = fileStorage.lastModified(1.toString())
        assert(lastModified != null)
        assert(lastModified!! in beforeCreation..afterCreation)

        Thread.sleep(10) // Ensure timestamp difference

        val beforeSave = System.currentTimeMillis()
        val updatedData = MockData(1, "UpdatedTest1")
        fileStorage.save(1.toString(), updatedData)
        val afterSave = System.currentTimeMillis()

        val lastModifiedAfterSave = fileStorage.lastModified(1.toString())
        assert(lastModifiedAfterSave != null)
        assert(lastModifiedAfterSave!! in beforeSave..afterSave)
        assert(lastModifiedAfterSave > lastModified)
    }

    @Test
    fun `Test lastModified on non existing id returns null`() {

        val lastModified = fileStorage.lastModified(1.toString())
        assert(lastModified == null)
    }

    @Test
    fun `Test loadAllIds returns correct ids`() {

        val idsBefore = fileStorage.loadAllIds()
        assert(idsBefore.isEmpty())

        fileStorage.new(1.toString()) { MockData(1, "Test1") }
        fileStorage.new(2.toString()) { MockData(2, "Test2") }

        val idsAfter = fileStorage.loadAllIds()
        assert(idsAfter.size == 2)
        assert(idsAfter.containsAll(listOf(1.toString(), 2.toString())))
    }

    @Test
    fun `Test loadAllIds after deletions`() {

        fileStorage.new(1.toString()) { MockData(1, "Test1") }
        fileStorage.new(2.toString()) { MockData(2, "Test2") }
        fileStorage.delete(1.toString())

        val ids = fileStorage.loadAllIds()
        assert(ids.size == 1)
        assert(ids.contains(2.toString()))
    }
}