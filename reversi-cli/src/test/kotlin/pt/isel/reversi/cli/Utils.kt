package pt.isel.reversi.cli

import pt.isel.reversi.utils.GAME_BASE_FOLDER
import java.io.File

fun cleanup(func: () -> Unit) {
    File(GAME_BASE_FOLDER).deleteRecursively()
    func()
    File(GAME_BASE_FOLDER).deleteRecursively()
}