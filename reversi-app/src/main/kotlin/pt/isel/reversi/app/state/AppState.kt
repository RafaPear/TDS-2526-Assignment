package pt.isel.reversi.app.state

import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.audio.AudioPool

/**
 * State of the application.
 * @param game The current game state.
 * @param page The current page being displayed.
 * @param error The current error, for remove error on display set to null.
 */
data class AppState(
    val game: Game,
    val page: Page,
    val error: ReversiException?,
    val backPage: Page = Page.MAIN_MENU,
    val audioPool: AudioPool,
)