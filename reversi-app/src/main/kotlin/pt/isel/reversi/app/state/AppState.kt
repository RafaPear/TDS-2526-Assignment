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
 */
data class AppState(
    val game: Game,
    val page: Page,
    val backPage: Page,
    val audioPool: AudioPool,
    val globalError: ReversiException?,
    val theme: AppTheme,
    val playerName: String?
) {
    companion object {
        // Empty AppState for initialization
        fun empty(): AppState = AppState(
            game = Game(),
            page = Page.MAIN_MENU,
            backPage = Page.MAIN_MENU,
            audioPool = AudioPool(emptyList()),
            globalError = null,
            theme = AppThemes.DARK.appTheme,
            playerName = null
        )
    }
}

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

interface ViewModel {
    val uiState: State<UiState>

    fun setError(error: Exception?)
}