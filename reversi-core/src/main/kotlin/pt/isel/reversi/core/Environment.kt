package pt.isel.reversi.core

import pt.isel.reversi.core.storage.serializers.GameStateSerializer
import pt.isel.reversi.utils.CORE_CONFIG_FILE
import pt.isel.reversi.storage.FileStorage
import pt.isel.reversi.utils.ConfigLoader

private val CONFIG: CoreConfig = ConfigLoader(CORE_CONFIG_FILE) { CoreConfig(it) }.loadConfig()

val BOARD_SIDE: Int = CONFIG.BOARD_SIDE
val TARGET_CHAR: Char = CONFIG.TARGET_CHAR
val EMPTY_CHAR: Char = CONFIG.EMPTY_CHAR
val SIDE_MIN: Int = CONFIG.SIDE_MIN
val SIDE_MAX: Int = CONFIG.SIDE_MAX
val SAVES_FOLDER: String = CONFIG.SAVES_FOLDER

/**
 * Default file-based storage used by the simple demo runner and tests. The folder is relative to the working
 * directory and defaults to `saves`.
 */
val STORAGE = FileStorage(
    folder = SAVES_FOLDER,
    serializer = GameStateSerializer()
)
