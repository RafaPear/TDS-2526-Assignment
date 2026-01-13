package pt.isel.reversi.app.state

import androidx.compose.runtime.State
import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.AppThemes
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

/**
 * Screen state for managing UI-level error and loading states.
 *
 * @property error The current error state, if any.
 * @property isLoading Whether the screen is currently loading.
 */
data class ScreenState(
    val error: ReversiException? = null,
    val isLoading: Boolean = false,
)

/**
 * Base class for UI state with screen state management.
 * Each subclass must implement updateScreenState to define how to copy itself with a new ScreenState.
 */
abstract class UiState {
    abstract val screenState: ScreenState

    /**
     * Creates a copy of this UiState with the given ScreenState.
     * Each subclass implements this using its data class copy() method.
     */
    abstract fun updateScreenState(newScreenState: ScreenState): UiState
}

/**
 * Contract for UI view models that manage screen state and error handling.
 *
 * @property uiState The current UI state as a reactive state holder.
 */
interface ViewModel {
    val uiState: State<UiState>

    /**
     * Sets or clears an error in the UI state.
     * @param error The exception to display, or null to clear the current error.
     */
    fun setError(error: Exception?)
}

/**
 * Represents the navigation state of the application.
 *
 * @property page The current page being displayed.
 * @property backPage The previous page for navigation history.
 */
data class PagesState(
    val page: Page,
    val backPage: Page,
)