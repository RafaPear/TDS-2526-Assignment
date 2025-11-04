package pt.isel.reversi.cli.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayCmdTests {

    @Test
    fun `parseCoordinateArgs accepts separated numbers`() {
        val c = PlayCmd.parseCoordinateArgs(listOf("3", "5"))
        assertEquals(3, c?.row)
        assertEquals(5, c?.col)
    }

    @Test
    fun `parseCoordinateArgs accepts combined numeric and letter (1A)`() {
        val c = PlayCmd.parseCoordinateArgs(listOf("1A"))
        assertEquals(1, c?.row)
        // 'A' -> col 1
        assertEquals(1, c?.col)
    }

    @Test
    fun `parseCoordinateArgs accepts combined digits (15)`() {
        val c = PlayCmd.parseCoordinateArgs(listOf("15"))
        assertEquals(1, c?.row)
        assertEquals(5, c?.col)
    }

    @Test
    fun `parseCoordinateArgs returns null for invalid inputs`() {
        assertNull(PlayCmd.parseCoordinateArgs(listOf("x")))
        assertNull(PlayCmd.parseCoordinateArgs(listOf("")))
        assertNull(PlayCmd.parseCoordinateArgs(listOf("1")))
    }
}

