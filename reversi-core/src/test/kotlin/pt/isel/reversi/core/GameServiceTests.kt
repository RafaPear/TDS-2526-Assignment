package pt.isel.reversi.core

import org.junit.Before
import pt.isel.reversi.core.gameServices.GameService
import kotlin.test.Test

class GameServiceTests {
    val service = GameService()

    @Before
    fun cleanUp() {
        // No-op for now
    }

    @Test
    fun missingTests() {
        assert(false)
    }
}