package pt.isel.reversi.app.pages.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import pt.isel.reversi.app.PreviousPage
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setPage

/**
 * Main game page displaying the Reversi board, player scores, and game controls.
 * Manages game music playback and periodic game state refreshes for multiplayer games.
 *
 * @param viewModel The game page view model containing UI state and game logic.
 * @param modifier Optional composable modifier for layout adjustments.
 * @param freeze Whether to freeze the game board and prevent user interaction.
 */
@Composable
fun GamePage(viewModel: GamePageViewModel, modifier: Modifier = Modifier, freeze: Boolean = false) {
    val appState = viewModel.appState
    val game = viewModel.uiState.value

    // Launch the game refresh coroutine
    DisposableEffect(viewModel) {
        if (game.currGameName != null && game.gameState?.players?.size != 2 && !viewModel.isPollingActive()) {
            viewModel.startPolling()
        }

        appState.getStateAudioPool().run {
            val theme = appState.value.theme
            if (!isPlaying(theme.gameMusic)) {
                stop(theme.backgroundMusic)
                stop(theme.gameMusic)
                play(theme.gameMusic)
            }
        }

        onDispose {
            viewModel.stopPolling()
            viewModel.save()
        }
    }

    val name = game.currGameName?.let { "Game: $it" }

    ScaffoldView(
        appState = appState,
        title = name ?: "Reversi",
        previousPageContent = {
            PreviousPage { appState.setPage(appState.value.backPage) }
        }
    ) { padding ->
        GamePageView(
            modifier = modifier.fillMaxSize()
                .background(appState.value.theme.backgroundColor)
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
