package pt.isel.reversi.app.pages.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.isel.reversi.app.*
import pt.isel.reversi.app.corroutines.launchGameRefreshCoroutine
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.exceptions.ReversiException

@Composable
fun GamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier, freeze: Boolean = false) {
    val coroutineAppScope = rememberCoroutineScope()

    // Launch the game refresh coroutine
    LaunchedEffect(appState.value.page) {
        val game = appState.value.game
        if (game.currGameName != null && game.gameState?.players?.size != 2) {
            launchGameRefreshCoroutine(50L, appState)
        }

        appState.getStateAudioPool().run {
            if (!isPlaying(MEGALOVANIA)) {
                stop(BACKGROUND_MUSIC)
                stop(MEGALOVANIA)
                play(MEGALOVANIA)
            }
        }
    }

    val name = appState.value.game.currGameName?.let { "Game: $it" }

    ScaffoldView(
        appState = appState,
        title = name ?: "Reversi",
        previousPageContent = {
            PreviousPage { appState.setPage(appState.value.backPage) }
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize()
                .background(BOARD_BACKGROUND_COLOR)
                .padding(paddingValues = padding)
                .testTag(tag = testTagGamePage()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            ) {
                Box(
                    modifier = modifier.weight(0.7f),
                ) {
                    DrawBoard(appState.value.game, freeze = freeze) { coordinate ->
                        coroutineAppScope.launch {
                            try {
                                appState.setGame(
                                    game = appState.value.game.play(coordinate)
                                )
                                appState.getStateAudioPool().run {
                                    stop(PLACE_PIECE_SOUND)
                                    play(PLACE_PIECE_SOUND)
                                }
                            } catch (e: ReversiException) {
                                appState.setError(error = e)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(width = 16.dp))

                // Coluna dos jogadores e botões
                Column(
                    modifier = modifier.weight(0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextPlayersScore(state = appState.value.game.gameState)

                    val target = appState.value.game.target

                    Spacer(modifier = Modifier.height(32.dp))


                    TargetButton(target, freeze = freeze) {
                        appState.setGame(
                            game = appState.value.game.setTargetMode(!appState.value.game.target)
                        )
                    }

                    //Spacer(modifier = Modifier.height(padding))

//                GameButton("Update", freeze = freeze) {
//                    appState.value = setGame(appState, appState.value.game.refresh())
//                }

                }
            }
        }
    }
}
