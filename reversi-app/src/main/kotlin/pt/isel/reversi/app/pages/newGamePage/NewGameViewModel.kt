package pt.isel.reversi.app.pages.newGamePage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import pt.isel.reversi.app.state.ScreenState
import pt.isel.reversi.app.state.UiState
import pt.isel.reversi.app.state.ViewModel
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.exceptions.ReversiException

data class NewGameUiState(
    override val screenState: ScreenState = ScreenState()
) : UiState() {
    override fun updateScreenState(newScreenState: ScreenState) =
        copy(screenState = newScreenState)
}

class NewGameViewModel(
    val scope: CoroutineScope,
    globalError: ReversiException? = null
): ViewModel  {
    private val _uiState = mutableStateOf(
        NewGameUiState(
            screenState = ScreenState(error = globalError)
        )
    )
    override val uiState: State<NewGameUiState> = _uiState

    override fun setError(error: Exception?) =
        _uiState.setError(error)
}