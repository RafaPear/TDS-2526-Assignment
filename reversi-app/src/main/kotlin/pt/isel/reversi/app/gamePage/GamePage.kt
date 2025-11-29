package pt.isel.reversi.app.gamePage

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
import pt.isel.reversi.app.PreviousPage
import pt.isel.reversi.app.ScaffoldView
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.BACKGROUND_MUSIC
import pt.isel.reversi.app.MEGALOVANIA
import pt.isel.reversi.app.PLACE_PIECE_SOUND
import kotlinx.coroutines.launch
import pt.isel.reversi.app.corroutines.launchGameRefreshCoroutine
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.app.state.setPage
import pt.isel.reversi.core.exceptions.ReversiException

@Composable
fun GamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier, freeze: Boolean = false) {
    val coroutineAppScope = rememberCoroutineScope()

    // Launch the game refresh coroutine
    LaunchedEffect(appState.value.page) {
        val game = appState.value.game
        if (game.currGameName != null && game.gameState?.players?.size != 2) {
            launchGameRefreshCoroutine(250L, appState)
        }

        val audioPool = getStateAudioPool(appState)
        if (!audioPool.isPlaying(MEGALOVANIA)) {
            audioPool.stop(BACKGROUND_MUSIC)
            audioPool.stop(MEGALOVANIA)
            audioPool.play(MEGALOVANIA)
        }
    }


    val name = appState.value.game.currGameName?.let { "Game: $it" }

    ScaffoldView(
        appState = appState,
        title = name ?: "Reversi",
        previousPageContent = {
            PreviousPage { appState.value = setPage(appState, appState.value.backPage) }
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
                                appState.value = setGame(
                                    appState,
                                    game = appState.value.game.play(coordinate)
                                )
                                val audioPool = getStateAudioPool(appState)
                                audioPool.stop(PLACE_PIECE_SOUND)
                                audioPool.play(PLACE_PIECE_SOUND)
                            } catch (e: ReversiException) {
                                appState.value = setError(appState, error = e)
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
                        appState.value = setGame(
                            appState,
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
