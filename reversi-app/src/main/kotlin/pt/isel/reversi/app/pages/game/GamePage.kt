package pt.isel.reversi.app.pages.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import pt.isel.reversi.app.BACKGROUND_MUSIC
import pt.isel.reversi.app.MEGALOVANIA
import pt.isel.reversi.app.PreviousPage
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.pages.game.utils.BOARD_BACKGROUND_COLOR
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setPage


@Composable
fun GamePage(viewModel: GamePageViewModel, modifier: Modifier = Modifier, freeze: Boolean = false) {
    val appState = viewModel.appState
    val game = viewModel.uiState.value

    // Launch the game refresh coroutine
    DisposableEffect(game.currGameName) {
        if (game.currGameName != null && game.gameState?.players?.size != 2) {
            viewModel.startPolling()
        }

        appState.getStateAudioPool().run {
            if (!isPlaying(MEGALOVANIA)) {
                stop(BACKGROUND_MUSIC)
                stop(MEGALOVANIA)
                play(MEGALOVANIA)
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
                .background(BOARD_BACKGROUND_COLOR)
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
