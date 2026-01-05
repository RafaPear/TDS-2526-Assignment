package pt.isel.reversi.app.pages.lobby

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.getTheme
import pt.isel.reversi.app.pages.lobby.lobbyViews.Empty
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.LobbyCarousel
import pt.isel.reversi.app.pages.lobby.lobbyViews.utils.PopupPickAPiece
import pt.isel.reversi.app.pages.lobby.lobbyViews.utils.RefreshButton
import pt.isel.reversi.app.reversiFadeAnimation
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.utils.LOGGER

enum class LobbyState {
    NONE, EMPTY, SHOW_GAMES
}

private const val PAGE_TRANSITION_DURATION_MS = 500

@Composable
fun LobbyMenu(
    viewModel: LobbyViewModel,
) {
    val uiState = viewModel.uiState.value
    val games = uiState.games
    val lobbyState = uiState.lobbyState
    val canRefresh = uiState.canRefresh
    val appState: MutableState<AppState> = viewModel.appState

    DisposableEffect(viewModel) {
        LOGGER.info("Starting polling for lobby updates.")
        viewModel.startPolling()
        onDispose {
            viewModel.stopPolling()
        }
    }

    val refreshAction: @Composable () -> Unit = {
        if (canRefresh) {
            RefreshButton {
                viewModel.refreshAll()
            }
        }
    }

    ScaffoldView(appState, title = "Lobby - Jogos Guardados") { padding ->
        val reversiScope = this
        AnimatedContent(
            targetState = lobbyState,
            transitionSpec = {
                val iOSEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                reversiFadeAnimation(PAGE_TRANSITION_DURATION_MS, iOSEasing)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(getTheme().backgroundColor),
            label = "PageTransition"
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = padding)
                    .padding(all = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LOGGER.info("LobbyMenu - Current page: $page with ${games.size} games.")
                when (page) {
                    LobbyState.NONE -> {}
                    LobbyState.EMPTY -> Empty(reversiScope) { refreshAction() }
                    LobbyState.SHOW_GAMES -> LobbyCarousel(
                        currentGameName = appState.value.game.currGameName,
                        games = games,
                        viewModel,
                        reversiScope = reversiScope,
                        buttonRefresh = { refreshAction() }
                    ) { game ->
                        viewModel.selectGame(game)
                    }
                }
            }

            viewModel.uiState.value.selectedGame?.let { game ->
                val state = viewModel.joinGameValidations(game)
                if (state == null) {
                    viewModel.selectGame(null)
                    return@let
                }
                val players = state.players.map { it.type }

                PopupPickAPiece(
                    pieces = players,
                    onPick = { pieceType ->
                        viewModel.joinGame(game, pieceType)
                    },
                    onDismiss = {
                        viewModel.selectGame(null)
                    }
                )
            }
        }
    }
}

fun testTagLobbyBoard() = "LobbyBoardPreview"
fun testTagCellPreview(coordinateIndex: Int) = "LobbyCellPreview_$coordinateIndex"
fun testTagCarouselItem(name: String) = "LobbyCarouselItem_$name"
