package pt.isel.reversi.app.lobbyMenuTests.lobbyCarouselViewsTests

import pt.isel.reversi.utils.BASE_FOLDER
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class LobbyCarouselTests {
    @BeforeTest
    @AfterTest
    fun cleanUp() {
        File(BASE_FOLDER).deleteRecursively()
    }

}