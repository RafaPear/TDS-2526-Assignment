package pt.isel.reversi.cli.commands

import pt.isel.reversi.cli.cleanup
import pt.isel.reversi.core.Game
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ListGamesCmdTests {
    @Test
    fun `ListGamesCmd reports no saved games when folder missing`() {
        cleanup {
            val res = ListGamesCmd.execute(context = Game())
            assertTrue(res.message.contains("No saved games"))
        }
    }

    @Test
    fun `ListGamesCmd lists files in saves folder`() {
        cleanup {
            val savesFolder = Game().config.savesPath
            File(savesFolder).mkdirs()
            File(savesFolder, "game1.json").writeText("x")
            File(savesFolder, "game2.json").writeText("y")

            val res = ListGamesCmd.execute(context = Game())
            assertTrue(
                res.message.contains("Available games") && res.message.contains("- game1") && res.message.contains(
                    "- game2"
                )
            )
        }
    }
}
