package pt.isel.reversi.app.state

import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.AppThemes
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.audio.AudioPool

/**
 * Central application state holding game, navigation, UI, and audio configuration.
 *
 * @property game The current game instance and state.
 * @property page The currently displayed page in the application.
 * @property error The current error exception to display, or null if no error.
 * @property backPage The previous page for back navigation (defaults to MAIN_MENU).
 * @property isLoading Whether the application is in a loading state.
 * @property audioPool The audio pool managing sound effects and background music.
 * @property theme The currently applied application theme.
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