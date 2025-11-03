package pt.isel.reversi.core

import pt.isel.reversi.utils.Config

class CoreConfig(map: Map<String, String>): Config {
    /** Side length of the game board. */
    val BOARD_SIDE = map["BOARD_SIDE"]?.toIntOrNull() ?: 8

    /** Character representing the target pieces on the board. */
    val TARGET_CHAR = map["TARGET_CHAR"]?.firstOrNull() ?: '*'

    /** Character representing empty spaces on the board. */
    val EMPTY_CHAR = map["EMPTY_CHAR"]?.firstOrNull() ?: '.'

    /** Minimum allowed board side length. */
    val SIDE_MIN = map["SIDE_MIN"]?.toIntOrNull() ?: 4

    /** Maximum allowed board side length. */
    val SIDE_MAX = map["SIDE_MAX"]?.toIntOrNull() ?: 26

    val SAVES_FOLDER = map["SAVES_FOLDER"] ?: "saves"

    override fun getDefaultConfigFileEntries(): Map<String, String> {
        return mapOf(
            "BOARD_SIDE" to BOARD_SIDE.toString(),
            "TARGET_CHAR" to TARGET_CHAR.toString(),
            "EMPTY_CHAR" to EMPTY_CHAR.toString(),
            "SIDE_MIN" to SIDE_MIN.toString(),
            "SIDE_MAX" to SIDE_MAX.toString(),
            "SAVES_FOLDER" to SAVES_FOLDER
        )
    }
}