package pt.isel.reversi.app.gamePage

import pt.isel.reversi.core.exceptions.ReversiException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.AppState
import pt.isel.reversi.app.setError
import pt.isel.reversi.app.setGame
import pt.isel.reversi.core.board.Coordinate

@Composable
fun GamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BOARD_BACKGROUND_COLOR)
            .padding(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (appState.value.game.currGameName != null)
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(BOARD_SIDE_COLOR, shape = RoundedCornerShape(12.dp))
                    .padding(10.dp),
            ) {
                DrawBoard(appState.value.game) { x, y ->
                    try {
                        appState.value = setGame(
                            appState,
                            game = appState.value.game.play(Coordinate(x, y))
                        )
                    } catch (e: ReversiException) {
                        appState.value = setError(appState, error = e)
                    }
                }
            }

            Spacer(modifier = Modifier.width(padding))

            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {

                TextPlayersScore(
                    state = appState.value.game.gameState,
                )

                Spacer(modifier = Modifier.height(padding))

                val target = if (appState.value.game.target) "On" else "Off"

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {// Button to toggle the target state
                    GameButton("Target $target") {
                        appState.value = setGame(
                            appState,
                            game = appState.value.game.setTargetMode(!appState.value.game.target)
                        )
                    }

                    Spacer(modifier = Modifier.height(padding))

                    // Main action button
                    GameButton("Update") {
                        appState.value = setGame(appState, appState.value.game.refresh())
                    }
                }
            }
        }
    }
}