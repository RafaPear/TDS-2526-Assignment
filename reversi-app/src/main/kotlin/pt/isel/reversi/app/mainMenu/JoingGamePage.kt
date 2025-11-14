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
import pt.isel.reversi.app.*
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.loadGame

@Composable
fun JoinGamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    var game by remember { mutableStateOf(appState.value.game) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Entrar num Jogo", fontSize = 28.sp, fontWeight = FontWeight.Bold)

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
            onClick = {
                if (game.currGameName.isNullOrBlank()) {
                    appState.value = setToastMessage(appState, message = "Insira um nome de jogo válido.")
                    return@Button
                }
                try {
                    game = loadGame(
                        gameName = game.currGameName!!.trim(),
                        desiredType = game.myPiece
                    )
                    println("Ligado ao jogo '$game'.")
                    appState.value = setAppState(appState, game, Page.GAME)
                } catch (e: Exception) {
                    appState.value = setToastMessage(appState, e.message ?: "Erro desconhecido")
                }
            }
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
fun ButtonPieceType(piece: PieceType, currentPiece: PieceType?, modifier: Modifier = Modifier, onClick: (PieceType) -> Unit) {
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

