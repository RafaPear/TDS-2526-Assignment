package pt.isel.reversi.core

import kotlin.test.Test

class CoreConfigTests {

    @Test
    fun `CoreConfig defaults when map empty`() {
        val cfg = CoreConfig(emptyMap())

        assert(cfg.BOARD_SIDE == 8)
        assert(cfg.TARGET_CHAR == '*')
        assert(cfg.EMPTY_CHAR == '.')
        assert(cfg.SIDE_MIN == 4)
        assert(cfg.SIDE_MAX == 26)
        assert(cfg.SAVES_FOLDER == "saves")
    }

    @Test
    fun `CoreConfig respects provided map values`() {
        val m = mapOf(
            "BOARD_SIDE" to "10",
            "TARGET_CHAR" to "T",
            "EMPTY_CHAR" to "_",
            "SIDE_MIN" to "6",
            "SIDE_MAX" to "20",
            "SAVES_FOLDER" to "my_saves"
        )
        val cfg = CoreConfig(m)

        assert(cfg.BOARD_SIDE == 10)
        assert(cfg.TARGET_CHAR == 'T')
        assert(cfg.EMPTY_CHAR == '_')
        assert(cfg.SIDE_MIN == 6)
        assert(cfg.SIDE_MAX == 20)
        assert(cfg.SAVES_FOLDER == "my_saves")
    }
}

