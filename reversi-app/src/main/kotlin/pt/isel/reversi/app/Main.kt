package pt.isel.reversi.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import org.jetbrains.compose.resources.painterResource
import pt.isel.reversi.app.gamePage.GamePage
import pt.isel.reversi.app.mainMenu.JoinGamePage
import pt.isel.reversi.app.mainMenu.MainMenu
import pt.isel.reversi.core.*
import pt.isel.reversi.core.board.PieceType
import reversi.reversi_app.generated.resources.Res
import reversi.reversi_app.generated.resources.reversi

data class AppState(
    val game: Game,
    val page: Page,
    val toastMessage: String?,
)

fun setGame(appState: MutableState<AppState>, game: Game) = appState.value.copy(game = game)


fun setPage(appState: MutableState<AppState>, page: Page) = appState.value.copy(page = page)


fun setToastMessage(appState: MutableState<AppState>, message: String?) = appState.value.copy(toastMessage = message)

fun setAppState(
    appState: MutableState<AppState>,
    game: Game = appState.value.game,
    page: Page = appState.value.page,
    toastMessage: String? = appState.value.toastMessage
) = AppState(game, page, toastMessage)


fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = WindowPosition.PlatformDefault
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "Reversi-DEV",
        icon = painterResource(Res.drawable.reversi),
        state = windowState
    ) {

        val appState = remember {
            mutableStateOf(
                AppState(
                    game = Game(),
                    page = Page.MAIN_MENU,
                    toastMessage = null
                )
            )
        }

        window.minimumSize = java.awt.Dimension(500, 500)


        MenuBar {
            Menu("Ficheiro") {
                Item("Novo Jogo") {
                    appState.value = setPage(appState, Page.NEW_GAME)
                }
                Item("Entrar em Jogo") {
                    appState.value = setPage(appState, Page.JOIN_GAME)
                }
                Item("Guardar Jogo") {
                    appState.value = setPage(appState, Page.SAVE_GAME)
                }
                Item("Definições") {
                    appState.value = setPage(appState, Page.SETTINGS)
                }
                Item("Menu Principal") {
                    appState.value = setPage(appState, Page.MAIN_MENU)
                }
                Item("Jogo Atual") {
                    appState.value = setPage(appState, Page.GAME)
                }
                Separator()
                Item("Sair") {
                    exitApplication()
                }
            }

            Menu("Dev") {
                Item("Mostrar Estado do Jogo") {
                    appState.value.game.printDebugState()
                }
            }

            Menu("Ajuda") {
                Item("Sobre") {
                    appState.value = setPage(appState, Page.ABOUT)
                }
            }
        }

        when (appState.value.page) {
            Page.MAIN_MENU -> MainMenu(appState)
            Page.GAME -> GamePage(appState)
            Page.SETTINGS -> SettingsPage(appState)
            Page.ABOUT -> AboutPage(appState)
            Page.JOIN_GAME -> JoinGamePage(appState)
            Page.NEW_GAME -> NewGamePage(appState)
            Page.SAVE_GAME -> SaveGamePage(appState)
        }
        appState.value.toastMessage?.let { ToastMessage(appState) }
    }
}


@Composable
fun ErrorDialog(appState: MutableState<AppState>, errorMessage: String, newPage: Page, onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = { appState.value = setPage(appState, newPage); onOk() },
        title = { Text("Erro") },
        text = { Text("Ocorreu um erro: $errorMessage") },
        confirmButton = {
            Button(
                onClick = {
                    appState.value = setPage(appState, newPage)
                    onOk()
                }
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun SaveGamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val gameName = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Guardar Jogo", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = gameName.value ?: "",
            onValueChange = { gameName.value = it },
            label = { Text("Nome do jogo") },
            singleLine = true
        )

        Button(
            onClick = {
                if (gameName.value?.isNotBlank() ?: false) {
                    try {
                        appState.value.game.saveGame()
                        appState.value = setPage(appState, Page.GAME)
                    } catch (e: Exception) {
                        appState.value = setToastMessage(appState, e.message ?: "Erro desconhecido")
                    }
                } else {
                    appState.value = setToastMessage(appState, "O nome do jogo não pode estar vazio.")
                }
            }
        ) {
            Text("Guardar")
        }

        Spacer(Modifier.height(10.dp))

        Button(onClick = { appState.value = setPage(appState, Page.GAME) }) {
            Text("Voltar")
        }
    }
}


@Composable
fun SettingsPage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Definições", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Text("Opções futuras: som, tema, rede, etc.")

        Spacer(Modifier.height(20.dp))

        Button(onClick = { appState.value = setPage(appState, Page.MAIN_MENU) }) {
            Text("Voltar")
        }
    }
}

@Composable
fun AboutPage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sobre", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Projeto Reversi desenvolvido no ISEL.")
        Text("Autores: Rafael Vermelho Pereira e equipa.")
        Text("Versão: DEV Build")

        Spacer(Modifier.height(20.dp))

        Button(onClick = { appState.value = setPage(appState, Page.MAIN_MENU) }) {
            Text("Voltar")
        }
    }
}


@Composable
fun NewGamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val gameNameState = remember { mutableStateOf<String?>(null) }
    val side = BOARD_SIDE
    val firstTurnState = mutableStateOf(PieceType.BLACK)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Novo Jogo", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = gameNameState.value ?: "",
            onValueChange = { gameNameState.value = it },
            label = { Text("Nome do jogo (opcional)") },
            singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Primeiro a jogar:")
            Button(onClick = { firstTurnState.value = PieceType.BLACK }) { Text("Preto") }
            Button(onClick = { firstTurnState.value = PieceType.WHITE }) { Text("Branco") }
        }

        Button(
            onClick = {
                try {
                    appState.value = setGame(
                        appState,
                        game = if (gameNameState.value?.ifBlank { null } != null) {
                            startNewGame(
                                side = side,
                                players = listOf(Player(firstTurnState.value)),
                                firstTurn = firstTurnState.value,
                                currGameName = gameNameState.value?.ifBlank { null }
                            )
                        } else {
                            startNewGame(
                                side = side,
                                players = listOf(
                                    Player(PieceType.BLACK),
                                    Player(PieceType.WHITE)
                                ),
                                firstTurn = firstTurnState.value
                            )
                        }
                    )
                    println("Novo jogo '${gameNameState.value?.ifBlank { "(local)" } ?: "(local)"} ' iniciado.")
                    appState.value = setPage( appState, Page.GAME)
                } catch (e: Exception) {
                    appState.value = setToastMessage( appState, e.message ?: "Erro desconhecido")
                }
            }
        ) {
            Text("Começar Jogo")
        }

        Spacer(Modifier.height(10.dp))

        Button(onClick = { appState.value = setPage( appState, Page.MAIN_MENU) }) {
            Text("Voltar")
        }
    }
}

fun Game.printDebugState() {
    println("========== ESTADO ATUAL DO JOGO ==========")
    println("Nome do jogo: ${currGameName ?: "(local)"}")
    println("Modo alvo (target): $target")
    println("Contagem de passes: $countPass")

    val state = gameState
    if (state == null) {
        println("⚠️ Sem estado de jogo carregado.")
        println("==========================================")
        return
    }

    println("\n--- Jogadores ---")
    state.players.forEachIndexed { i, player ->
        println("Jogador ${i + 1}: ${player.type} (${player.points} pontos)")
    }

    println("Último jogador: ${state.lastPlayer}")
    println("Vencedor: ${state.winner?.type ?: "Nenhum"}")

    val board = state.board
    println("\n--- Tabuleiro ---")
    println("Tamanho: ${board.side}x${board.side}")
    println("Peças pretas: ${board.totalBlackPieces}, Peças brancas: ${board.totalWhitePieces}")
    println("Representação:")
    println(this.stringifyBoard())

    println("==========================================\n")
}

