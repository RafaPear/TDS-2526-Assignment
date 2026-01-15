package pt.isel.reversi.app.state

import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.AppThemes
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.app.pages.PagesState
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.audio.AudioPool

/**
 * Central application state with each field as a mutable state.
 * This allows fine-grained reactivity: changing one field only invalidates
 * composables that read that specific field.
 *
 * @property game The current game instance.
 * @property pagesState The current page and navigation state.
 * @property audioPool The audio pool for managing game sounds.
 * @property globalError A global error that occurred in the application, if any.
 * @property theme The current application theme.
 * @property playerName The name of the current player, if set.
 */
data class AppState(
    val game: Game,
    val pagesState: PagesState,
    val audioPool: AudioPool,
    val globalError: ReversiException?,
    val theme: AppTheme,
    val playerName: String?
) {
    companion object {
        // Empty AppState for initialization
        fun empty(): AppState = AppState(
            game = Game(),
            pagesState = PagesState(Page.MAIN_MENU, Page.NONE),
            audioPool = AudioPool(emptyList()),
            globalError = null,
            theme = AppThemes.DARK.appTheme,
            playerName = null
        )
    }
}