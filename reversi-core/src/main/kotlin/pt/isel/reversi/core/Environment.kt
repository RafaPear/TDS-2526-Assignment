package pt.isel.reversi.core

import pt.isel.reversi.utils.CORE_CONFIG_FILE
import pt.isel.reversi.utils.ConfigLoader

/** Minimum allowed board side length. */
const val SIDE_MIN = 4

/** Maximum allowed board side length. */
const val SIDE_MAX = 26

fun loadCoreConfig(): CoreConfig = ConfigLoader(CORE_CONFIG_FILE) {
    CoreConfig(it)
}.loadConfig()