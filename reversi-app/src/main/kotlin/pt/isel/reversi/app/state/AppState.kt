package pt.isel.reversi.app.state

import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.AppThemes
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
    val isLoading: Boolean = false,
    val audioPool: AudioPool,
    val theme: AppTheme,
){
    companion object {
        // Empty AppState for initialization
        val EMPTY_APP_STATE = AppState(
            game = Game(),
            page = Page.MAIN_MENU,
            error = null,
            audioPool = AudioPool(emptyList()),
            theme = AppThemes.DARK.appTheme
        )
    }
}