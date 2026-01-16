package pt.isel.reversi.app.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import pt.isel.reversi.app.app.state.setError
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException

abstract class ViewModel<T : UiState> {
    protected abstract val _uiState: MutableState<T>
    val uiState: State<T> get() = _uiState
    abstract val globalError: ReversiException?
    abstract val setGlobalError: (Exception?, ErrorType?) -> Unit

    // get() vai sempre buscar ao uiState. Se o uiState for alterado, o error também é alterado
    val error get() = globalError ?: uiState.value.screenState.error

    fun setError(error: Exception?, type: ErrorType? = ErrorType.WARNING) {
        if (globalError != null) {
            setGlobalError(error, type)
        } else {
            _uiState.setError(error, type ?: ErrorType.WARNING)
        }
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
interface UiState {
    val screenState: ScreenState

    /**
     * Creates a copy of this UiState with the given ScreenState.
     * Each subclass implements this using its data class copy() method.
     */
    fun updateScreenState(newScreenState: ScreenState): UiState
}
