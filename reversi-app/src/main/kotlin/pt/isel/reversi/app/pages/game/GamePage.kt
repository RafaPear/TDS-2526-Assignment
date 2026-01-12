package pt.isel.reversi.app.pages.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import pt.isel.reversi.app.PreviousPage
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.core.Game
import pt.isel.reversi.utils.TRACKER

/**
 * Main game page displaying the Reversi board, player scores, and game controls.
 * Manages game music playback and periodic game state refreshes for multiplayer games.
 *
 * @param viewModel The game page view model containing UI state and game logic.
 * @param modifier Optional composable modifier for layout adjustments.
 * @param freeze Whether to freeze the game board and prevent user interaction.
 */
@Composable
fun ReversiScope.GamePage(
    viewModel: GamePageViewModel,
    modifier: Modifier = Modifier,
    freeze: Boolean = false,
    onLeave: (Game) -> Unit,
) {
    val game = viewModel.uiState.value.game
    val theme = appState.theme

    // Launch the game refresh coroutine
    DisposableEffect(viewModel) {
        TRACKER.trackEffectStart(this)
        if (game.currGameName != null && !viewModel.isPollingActive()) {
            viewModel.startPolling()
        }

        appState.audioPool.run {
            if (!isPlaying(theme.gameMusic)) {
                stop(theme.backgroundMusic)
                stop(theme.gameMusic)
                play(theme.gameMusic)
            }
        }

        onDispose {
            viewModel.stopPolling()
            viewModel.save()
            TRACKER.trackEffectStop(this)
        }
    }

    val name = game.currGameName

    this.ScaffoldView(
        setError = { viewModel.setError(it) },
        error = viewModel.uiState.value.screenState.error,
        isLoading = viewModel.uiState.value.screenState.isLoading,
        title = name ?: "Reversi",
        previousPageContent = {
            PreviousPage { onLeave(game) }
        }
    ) { padding ->
        GamePageView(
            modifier = modifier.fillMaxSize()
                .padding(paddingValues = padding),
            game = game,
            freeze = freeze,
            getAvailablePlays = { viewModel.getAvailablePlays() },
            onCellClick = { viewModel.playMove(it) },
            setTargetMode = { viewModel.setTarget(!game.target) },
            pass = { viewModel.pass() }
        )
    }
}
