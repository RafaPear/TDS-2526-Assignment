package pt.isel.reversi.app.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException

/**
 * Represents the different pages in the application along with their hierarchy levels.
 * @property level The hierarchy level of the page, where a higher number indicates a deeper level.
 */
enum class Page(val level: Int) {
    NONE(-1),
    MAIN_MENU(0),
    SETTINGS(1),
    ABOUT(1),
    NEW_GAME(1),

    //SAVE_GAME(1),
    LOBBY(1),
    GAME(2),
    STATISTICS(1),
    WINNER(3)
}


data class ScreenState(
    val error: ReversiException? = null,
    val isLoading: Boolean = false,
)

/**
 * Base class for UI state with screen state management.
 * Each subclass must implement updateScreenState to define how to copy itself with a new ScreenState.
 */
interface UiState {
    val screenState: ScreenState

    /**
     * Creates a copy of this UiState with the given ScreenState.
     * Each subclass implements this using its data class copy() method.
     */
    fun updateScreenState(newScreenState: ScreenState): UiState
}

abstract class ViewModel<T : UiState> {
    protected abstract val _uiState: MutableState<T>
    abstract val uiState: State<T>
    protected abstract val globalError: ReversiException?
    protected abstract val setGlobalError: (Exception?) -> Unit

    fun setError(error: Exception?, type: ErrorType = ErrorType.WARNING) {
        if (globalError != null) {
            setGlobalError(error)
        } else {
            _uiState.setError(error, type)
        }
    }
}

data class PagesState(
    val page: Page,
    val backPage: Page,
)
