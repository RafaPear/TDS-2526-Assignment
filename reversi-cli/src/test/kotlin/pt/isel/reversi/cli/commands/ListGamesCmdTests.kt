package pt.isel.reversi.cli.commands

import pt.isel.reversi.cli.cleanup
import pt.isel.reversi.core.SAVES_FOLDER
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ListGamesCmdTests {
    @Test
    fun `ListGamesCmd reports no saved games when folder missing`() {
        cleanup {
            val res = ListGamesCmd.execute(context = null)
            assertTrue(res.message.contains("No saved games"))
        }
    }

    @Test
    fun `ListGamesCmd lists files in saves folder`() {
        cleanup {
            File(SAVES_FOLDER).mkdirs()
            File(SAVES_FOLDER, "game1.json").writeText("x")
            File(SAVES_FOLDER, "game2.json").writeText("y")

            val res = ListGamesCmd.execute(context = null)
            assertTrue(
                res.message.contains("Available games") && res.message.contains("- game1") && res.message.contains(
                    "- game2"
                )
            )
        }
    }
}
