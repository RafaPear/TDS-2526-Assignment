package pt.isel.reversi.app.pages.menu

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.exceptions.ReversiException


data class MainMenuUIState(
    override val screenState: ScreenState = ScreenState()
) : UiState() {
    override fun updateScreenState(newScreenState: ScreenState): UiState {
        return this.copy(screenState = newScreenState)
    }
}

class MainMenuViewModel(
    private val appState: AppState,
    private val globalError: ReversiException? = null,
    private val setGlobalError: (Exception?) -> Unit,
) : ViewModel {
    private val _uiState = mutableStateOf(
        MainMenuUIState(
            screenState = ScreenState(
                error = globalError
            )
        )
    )
    override val uiState: State<MainMenuUIState> = _uiState

    override fun setError(error: Exception?) {
        if (globalError != null) {
            setGlobalError(error)
        } else
            _uiState.setError(error)
    }

    fun playMenuAudio() {
        val audioPool = getStateAudioPool(appState)
        val theme = appState.theme
        if (!audioPool.isPlaying(theme.backgroundMusic)) {
            audioPool.stopAll()
            audioPool.play(theme.backgroundMusic)
        }
    }
}