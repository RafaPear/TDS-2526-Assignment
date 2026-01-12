package pt.isel.reversi.app.pages.menu

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import pt.isel.reversi.app.state.ScreenState
import pt.isel.reversi.app.state.UiState
import pt.isel.reversi.app.state.ViewModel
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.exceptions.ReversiException


data class MainMenuUIState(
    override val screenState: ScreenState = ScreenState()
) : UiState() {
    override fun updateScreenState(newScreenState: ScreenState): UiState {
        return this.copy(screenState = newScreenState)
    }
}

class MainMenuViewModel(
    val scope: CoroutineScope,
    globalError: ReversiException?
): ViewModel {
    private val _uiState = mutableStateOf(
        MainMenuUIState(
            screenState = ScreenState(
                error = globalError
            )
        )
    )
    override val uiState: State<MainMenuUIState> = _uiState

    override fun setError(error: Exception?) {
        _uiState.setError(error)
    }
}