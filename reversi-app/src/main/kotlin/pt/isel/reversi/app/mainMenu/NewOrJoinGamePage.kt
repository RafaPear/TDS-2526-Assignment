package pt.isel.reversi.app.mainMenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.HIT_SOUND
import pt.isel.reversi.app.exceptions.NoPieceSelected
import pt.isel.reversi.app.exceptions.TextBoxIsEmpty
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.loadGame
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.utils.LOGGER

@Composable
fun NewGamePage(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier,
) = newOrJoinGamePage(appState, modifier, "Novo Jogo", onClick = { game ->
    val currGameName = game.currGameName

    val myPiece: PieceType = game.myPiece ?: run {
        appState.value = setError(appState, error = NoPieceSelected())
        return@newOrJoinGamePage
    }

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
        appState.value = setAppState(appState, newGame, Page.GAME)
        getStateAudioPool(appState).play(HIT_SOUND)
    } catch (e: ReversiException) {
        appState.value = setError(appState, e)
    }
})

@Composable
fun JoinGamePage(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier,
) = newOrJoinGamePage(appState, modifier, "Entrar num Jogo", onClick = { game ->
    val currGameName = game.currGameName
    if (currGameName.isNullOrBlank()) {
        appState.value = setError(
            appState,
            error = TextBoxIsEmpty(
                message = "Insira um nome do jogo.",
                type = ErrorType.INFO
            )
        )
        return@newOrJoinGamePage
    }
    try {
        val loadedGame = loadGame(
            gameName = currGameName.trim(),
            desiredType = game.myPiece
        )
        LOGGER.info("Ligado ao jogo '$loadedGame'.")
        appState.value = setAppState(appState, loadedGame, Page.GAME)
    } catch (e: ReversiException) {
        appState.value = setError(appState, e)
    }
})

@Composable
private fun newOrJoinGamePage(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier,
    title: String,
    onClick: (Game) -> Unit
) {
    var game by remember { mutableStateOf(appState.value.game) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            modifier = modifier,
            value = game.currGameName ?: "",
            onValueChange = { game = game.copy(currGameName = it) },
            label = { Text("Nome do jogo") },
            singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sou o jogador:")

            ButtonPieceType(PieceType.BLACK, game.myPiece) { selected ->
                game = game.changeMyPiece(selected)
            }

            ButtonPieceType(PieceType.WHITE, game.myPiece) { selected ->
                game = game.changeMyPiece(selected)
            }
        }

        Button(
            modifier = modifier,
            onClick = { onClick(game) }
        ) {
            Text("Entrar")
        }

        Spacer(Modifier.height(10.dp))

        Button(onClick = { appState.value = setPage(appState, Page.MAIN_MENU) }) {
            Text("Voltar")
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
        border = if (currentPiece == piece) BorderStroke(2.dp, Color.Green) else null
    ) {
        Text(
            text = when (piece) {
                PieceType.BLACK -> "Preto"
                PieceType.WHITE -> "Branco"
            },
            color = if (currentPiece == piece) Color.Green else Color.White
        )
    }
}

