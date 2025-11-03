package pt.isel.reversi.cli

import pt.isel.reversi.utils.CONFIG_FOLDER
import java.io.File


fun cleanup(func: () -> Unit) {
    File(CONFIG_FOLDER).deleteRecursively()
    File("saves").deleteRecursively()
    func()
    File(CONFIG_FOLDER).deleteRecursively()
    File("saves").deleteRecursively()
}