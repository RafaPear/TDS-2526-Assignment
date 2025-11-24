package pt.isel.reversi.app.gamePage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.BACKGROUND_MUSIC
import pt.isel.reversi.app.MEGALOVANIA
import pt.isel.reversi.app.corroutines.launchGameRefreshCoroutine
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.core.exceptions.ReversiException

@Composable
fun GamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier, freeze: Boolean = false) {
    val coroutineAppScope = rememberCoroutineScope()
    // Launch the game refresh coroutine
    coroutineAppScope.launchGameRefreshCoroutine(250L, appState)
    val game = appState.value.game
    if (game.currGameName != null && game.gameState?.players?.size != 2) {
        coroutineAppScope.launchGameRefreshCoroutine(1000L, appState)
    }

    val audioPool = getStateAudioPool(appState)
    if (!audioPool.isPlaying(MEGALOVANIA)) {
        audioPool.stop(BACKGROUND_MUSIC)
        audioPool.stop(MEGALOVANIA)
        audioPool.play(MEGALOVANIA)
    }

    if (!getStateAudioPool(appState).isPlaying(MEGALOVANIA)) {
        getStateAudioPool(appState).stop(BACKGROUND_MUSIC)
        getStateAudioPool(appState).stop(MEGALOVANIA)
        getStateAudioPool(appState).play(MEGALOVANIA)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BOARD_BACKGROUND_COLOR)
            .padding(all = 10.dp)
            .testTag(tag = testTagGamePage())
    ) {
        val name = appState.value.game.currGameName
        Row(
            modifier = modifier.fillMaxWidth().testTag(tag = testTagTitle(gameName = name)),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (name != null) {
                Text(
                    text = "Game: $name",
                    color = TEXT_COLOR,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 50.sp
                    ),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }

        Spacer(modifier = Modifier.height(padding))

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = modifier.weight(0.7f),
            ) {
                DrawBoard(appState.value.game, freeze = freeze) { coordinate ->
                    try {
                        appState.value = setGame(
                            appState,
                            game = appState.value.game.play(coordinate)
                        )
                        val audioPool = getStateAudioPool(appState)
                        audioPool.stop("putPiece")
                        audioPool.play("putPiece")
                    } catch (e: ReversiException) {
                        appState.value = setError(appState, error = e)
                    }
                }
            }

            Spacer(modifier = Modifier.width(padding))

            // Coluna dos jogadores e botões
            Column(
                modifier = modifier.weight(0.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextPlayersScore(state = appState.value.game.gameState)

                Spacer(modifier = Modifier.height(padding * 3))

                val target = appState.value.game.target

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