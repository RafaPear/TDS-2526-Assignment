package pt.isel.reversi.app.app.state

import pt.isel.reversi.app.app.AppTheme
import pt.isel.reversi.app.app.AppThemes
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.gameServices.GameServiceImpl
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
    override val gameSession: GameSession,
    override val pagesState: PagesState,
    override val audioThemeState: AudioThemeState,
    private val serviceC: GameServiceImpl = gameSession.game.service
) : AppStateImpl {
    override val service get() = game.service

    companion object {
        // Empty AppState for initialization
        fun empty(service: GameServiceImpl): AppState = AppState(
            gameSession = GameSession(Game(service = service), null),
            pagesState = PagesState(Page.MAIN_MENU, Page.NONE, null),
            audioThemeState = AudioThemeState(
                audioPool = AudioPool(emptyList()),
                theme = AppThemes.DARK.appTheme
            )
        )
    }
}

data class PagesState(
    val page: Page,
    val backPage: Page,
    val globalError: ReversiException?,
)


data class GameSession(val game: Game, val playerName: String?)
data class AudioThemeState(val audioPool: AudioPool, val theme: AppTheme)

