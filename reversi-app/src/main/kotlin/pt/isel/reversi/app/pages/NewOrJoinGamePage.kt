package pt.isel.reversi.app.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.isel.reversi.app.*
import pt.isel.reversi.app.exceptions.NoPieceSelected
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.utils.LOGGER

@Composable
fun NewGamePage(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier,
) {
    val coroutineAppScope = rememberCoroutineScope()
    newOrJoinGamePage(appState, modifier, "Novo Jogo") { game ->
        val currGameName = game.currGameName

        val myPiece: PieceType = game.myPiece ?: run {
            appState.setError(error = NoPieceSelected())
            return@newOrJoinGamePage
        }

        coroutineAppScope.launch {
            try {
                val newGame = if (currGameName.isNullOrBlank()) {
                    startNewGame(
                        players = listOf(
                            Player(PieceType.BLACK),
                            Player(PieceType.WHITE)
                        ),
                        firstTurn = myPiece,
                    )
                } else {
                    startNewGame(
                        players = listOf(
                            Player(myPiece)
                        ),
                        firstTurn = myPiece,
                        currGameName = currGameName.trim()
                    )
                }

                LOGGER.info("Novo jogo '${currGameName?.ifBlank { "(local)" } ?: "(local)"} ' iniciado.")
                appState.run {
                    setAppState(newGame, Page.GAME)
                    getStateAudioPool().play(HIT_SOUND)
                }
            } catch (e: Exception) {
                appState.setError(e)
            }
        }
    }
}

@Composable
private fun newOrJoinGamePage(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier,
    title: String,
    onClick: (Game) -> Unit
) {
    var game by remember { mutableStateOf(appState.value.game) }
    ScaffoldView(
        appState = appState,
        title = title,
        previousPageContent = {
            PreviousPage { appState.setPage(Page.MAIN_MENU) }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            //  colors: TextFieldColors =
            ReversiTextField(
                modifier = modifier,
                value = game.currGameName ?: "",
                onValueChange = { game = game.copy(currGameName = it) },
                label = { Text("Nome do jogo", color = TEXT_COLOR) },
                singleLine = true,
                onDone = { onClick(game); LOGGER.info("Nome do jogo") },
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sou o jogador:", color = TEXT_COLOR)

                ButtonPieceType(PieceType.BLACK, game.myPiece) { selected ->
                    game = game.changeMyPiece(selected)
                }

                ButtonPieceType(PieceType.WHITE, game.myPiece) { selected ->
                    game = game.changeMyPiece(selected)
                }
            }

            Button(
                modifier = modifier,
                onClick = { onClick(game) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = PRIMARY
                )
            ) {
                Text("Entrar", color = TEXT_COLOR)
            }
        }
    }
}

@Composable
fun ButtonPieceType(
    piece: PieceType,
    currentPiece: PieceType?,
    modifier: Modifier = Modifier,
    onClick: (PieceType) -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onClick(piece) },
        border = if (currentPiece == piece) BorderStroke(2.dp, Color.White) else null,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = PRIMARY
        )
    ) {
        Text(
            text = when (piece) {
                PieceType.BLACK -> "Preto"
                PieceType.WHITE -> "Branco"
            },
            color = TEXT_COLOR
        )
    }
}

