package pt.isel.reversi

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer

fun editLines(path: Path, operator: (String) -> String) {
    val fs = FileSystem.SYSTEM
    val tempPath = (path.parent ?: FileSystem.SYSTEM_TEMPORARY_DIRECTORY) / "temp_${path.name}"

    fs.write(tempPath) {
        val sink = this.buffer
        fs.source(path).buffer().use { source ->
            while (true) {
                val line = source.readUtf8Line() ?: break
                sink.writeUtf8(operator(line))
                sink.writeUtf8("\n")
            }
        }
        sink.flush()
    }

    fs.atomicMove(tempPath, path) // substitui o ficheiro original pelo temporário
}

fun main() {
    val filePath = "game.txt".toPath()
    editLines(filePath) { line ->
        if ("piece" in line) line.replace("piece", "token") else line
    }
}
