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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.corroutines.launchGameRefreshCoroutine
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER

@Composable
fun GamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier, freeze: Boolean = false) {
    val coroutineAppScope = rememberCoroutineScope()
    // Launch the game refresh coroutine
    coroutineAppScope.launchGameRefreshCoroutine(1000L, appState)

    LOGGER.info("Rendering GamePage")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BOARD_BACKGROUND_COLOR)
            .padding(10.dp),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (appState.value.game.currGameName != null && !freeze)
                Text(
                    text = "Game: ${appState.value.game.currGameName}",
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

                val target = if (appState.value.game.target) "On" else "Off"

                GameButton("Target $target", freeze = freeze) {
                    appState.value = setGame(
                        appState,
                        game = appState.value.game.setTargetMode(!appState.value.game.target)
                    )
                }

                Spacer(modifier = Modifier.height(padding))

//                GameButton("Update", freeze = freeze) {
//                    appState.value = setGame(appState, appState.value.game.refresh())
//                }

            }
        }
    }
}