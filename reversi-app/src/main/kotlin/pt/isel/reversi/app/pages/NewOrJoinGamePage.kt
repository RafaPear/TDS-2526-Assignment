package pt.isel.reversi.app.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.isel.reversi.app.*
import pt.isel.reversi.app.exceptions.NoPieceSelected
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.setAppState
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.utils.LOGGER

/**
 * New game page for creating a local game with piece selection.
 * Allows the user to choose their piece color and start a new game.
 *
 * @param appState Global application state for navigation and game control.
 */
@Composable
fun NewGamePage(
    appState: MutableState<AppState>,
) {
    val coroutineAppScope = rememberCoroutineScope()
    val reversiScope = ReversiScope(appState.value)
    with(reversiScope) {
        NewOrJoinGamePage("Novo Jogo", appState) { game ->
            val currGameName = game.currGameName

            val myPiece: PieceType = game.myPiece ?: run {
                appState.setError(error = NoPieceSelected())
                return@NewOrJoinGamePage
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
                    }
                } catch (e: Exception) {
                    appState.setError(e)
                }
            }
        }
    }
}

/**
 * Shared UI for new/join game pages with piece selection and game initiation.
 * Provides a dropdown menu for piece selection and a button to start the game.
 *
 * @param title The title displayed at the top of the page.
 * @param appState Global application state for navigation and game control.
 * @param onClick Callback invoked when the user confirms game creation with selected piece.
 */
@Composable
private fun ReversiScope.NewOrJoinGamePage(
    title: String,
    appState: MutableState<AppState>,
    onClick: (Game) -> Unit
) {
    var game by remember { mutableStateOf(getCurrentState().game) }
    var expanded by remember { mutableStateOf(false) }
    val theme = getTheme()

    ScaffoldView(
        appState = appState,
        title = title,
        previousPageContent = {
            PreviousPage { appState.setAppState(page = Page.MAIN_MENU) }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReversiTextField(
                    value = game.currGameName ?: "",
                    onValueChange = { game = game.copy(currGameName = it) },
                    label = { ReversiText("Nome do jogo",) },
                    modifier = Modifier.fillMaxWidth(),
                    onDone = { onClick(game) },
                )


                val selectedPieceLabel = remember(game.myPiece) {
                    when (game.myPiece) {
                        PieceType.BLACK -> "Preto"
                        PieceType.WHITE -> "Branco"
                        else -> "Selecionar cor..."
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    ReversiText(
                        "Minha peça:",
                        color = theme.textColor.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    Box(modifier = Modifier.run { fillMaxWidth() }) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, theme.textColor.copy(0.2f)),

                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.textColor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ReversiText(selectedPieceLabel,)
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = theme.textColor
                                )
                            }
                        }

                        ReversiDropDownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.widthIn(min = 200.dp)
                        ) {
                            ReversiDropdownMenuItem(
                                text = "Preto",
                                onClick = {
                                    game = game.changeMyPiece(PieceType.BLACK)
                                    expanded = false
                                }
                            )
                            ReversiDropdownMenuItem(
                                text = "Branco",
                                onClick = {
                                    game = game.changeMyPiece(PieceType.WHITE)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                ReversiButton(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    onClick = { onClick(game) },
                    text = "Entrar"
                )
            }
        }
    }
}

@Composable
fun ReversiScope.ButtonPieceType(
    piece: PieceType,
    currentPiece: PieceType?,
    modifier: Modifier = Modifier,
    onClick: (PieceType) -> Unit
) {
    ReversiButton(
        modifier = modifier,
        onClick = { onClick(piece) },
        border = if (currentPiece == piece) BorderStroke(2.dp, Color.White) else null,
        text = when (piece) {
            PieceType.BLACK -> "Preto"
            PieceType.WHITE -> "Branco"
        }
    )
}

