package pt.isel.reversi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import org.jetbrains.compose.resources.painterResource
import pt.isel.reversi.app.exceptions.ErrorMessage
import pt.isel.reversi.app.gamePage.GamePage
import pt.isel.reversi.app.mainMenu.JoinGamePage
import pt.isel.reversi.app.mainMenu.MainMenu
import pt.isel.reversi.app.mainMenu.NewGamePage
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.stringifyBoard
import pt.isel.reversi.utils.LOGGER
import reversi.reversi_app.generated.resources.Res
import reversi.reversi_app.generated.resources.reversi

fun main(args: Array<String>) {
    val initializedArgs = initializeAppArgs(args) ?: return
    val (audioPool) = initializedArgs

    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.PlatformDefault
        )

        val appState = remember {
            mutableStateOf(
                AppState(
                    game = Game(),
                    page = Page.MAIN_MENU,
                    error = null,
                    audioPool = audioPool
                )
            )
        }

        fun safeExitApplication() {
            LOGGER.info("Exiting application...")

            try {
                appState.value.game.saveEndGame()
                getStateAudioPool(appState).destroy()
            } catch (e: ReversiException) {
                LOGGER.warning("Failed to save game on exit: ${e.message}")
            }

            exitApplication()
        }

        Window(
            onCloseRequest = ::safeExitApplication,
            title = "Reversi-DEV",
            icon = painterResource(Res.drawable.reversi),
            state = windowState
        ) {

            window.minimumSize = java.awt.Dimension(500, 500)

            MakeMenuBar(appState, ::safeExitApplication)

            Box(modifier = Modifier.fillMaxSize()) {
                when (appState.value.page) {
                    Page.MAIN_MENU -> MainMenu(appState)
                    Page.GAME -> GamePage(appState)
                    Page.SETTINGS -> SettingsPage(appState)
                    Page.ABOUT -> AboutPage(appState)
                    Page.JOIN_GAME -> JoinGamePage(appState)
                    Page.NEW_GAME -> NewGamePage(appState)
                    Page.SAVE_GAME -> SaveGamePage(appState)
                }

                // Show error dialog if there is an error
                appState.value.error?.let { ErrorMessage(appState) }
                Box (modifier = Modifier
                    .padding(all = 24.dp)
                    .align(Alignment.BottomEnd)
                ) {
                    if (appState.value.page != Page.MAIN_MENU) {
                        PreviousPage {
                            appState.value = setPage(appState, appState.value.backPage)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Page to save the current game.
 * Save only board state and last player, not players info
 * (for avoid conflicts, because if save players info, in current game, permit other person to play with same piece type).
 * If the game has no name, allows the user to enter a name.
 * If the game has a name, shows the name but does not allow editing.
 * When the user clicks the save button, saves the game and returns to the game page.
 */
@Composable
fun SaveGamePage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val game = appState.value.game
    var gameName by remember { mutableStateOf(game.currGameName) }
    GamePage(appState, freeze = true)
    Column(
        modifier = modifier
            .fillMaxSize().background(Color.White.copy(alpha = 0.5f))
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Guardar Jogo", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = gameName ?: "",
            enabled = appState.value.game.currGameName == null,
            onValueChange = { gameName = it },
            label = { Text("Nome do jogo") },
            singleLine = true
        )

        Button(
            onClick = {
                appState.value = setGame(
                    appState,
                    game.copy(currGameName = gameName?.trim() ?: return@Button)
                )
                try {
                    appState.value.game.saveOnlyBoard(gameState = appState.value.game.gameState)
                    appState.value = setPage(appState, Page.GAME)
                } catch (e: ReversiException) {
                    appState.value = setAppState(
                        appState, error = e,
                        game = game.copy(currGameName = null)
                    )
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
        val currentMasterVolume = getStateAudioPool(appState).getMasterVolume()
        if (currentMasterVolume == null) LOGGER.warning("Master volume is null, using 0f as default")
        var volume by remember { mutableStateOf(currentMasterVolume ?: 0f) }

        // Convert volume in dB [-20, 0] to percentage [0, 100]
        val number = if (volume == 0f) " (Default)" else if (volume == -20f) " (disabled)" else " (${volumeDbToPercent(volume, 20f, 0f)}%)"

        Text("Master Volume: $number",
             fontSize = 20.sp,
             fontWeight = FontWeight.Medium
        )
        Slider(value = volume, valueRange = -20f..0f, onValueChange = {
            volume = it
            val audioPool = getStateAudioPool(appState)
            if (volume == -20f) {
                audioPool.mute(true)
            } else {
                audioPool.mute(false)
                audioPool.setMasterVolume(volume)
            }
        })

        Spacer(Modifier.height(20.dp))

        Button(onClick = { appState.value = setPage(appState, Page.MAIN_MENU) }) {
            Text("Voltar")
        }
    }
}

/**
 * Converts a volume in decibels to a percentage string representation (0-100).
 * @param volume The volume in decibels.
 * @param min The minimum volume in decibels, defining the lower bound of the conversion range (default -20f).
 * @param max The maximum volume in decibels, defining the upper bound of the conversion range (default 0f).
 * @return A string representation of the volume as a percentage (0-100).
 */
fun volumeDbToPercent(volume: Float, min: Float, max: Float): String {
    val percent = ((volume - min) / (max - min)) * 100
    return percent.toInt().toString()
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
        Text("Autores: ")
        Text(" - Rafael Pereira - NUMERO")
        Text(" - Ian Frunze - NUMERO")
        Text(" - Tito Silva - NUMERO")
        Text("Versão: DEV Build")

        Spacer(Modifier.height(20.dp))

        Button(onClick = { appState.value = setPage(appState, Page.MAIN_MENU) }) {
            Text("Voltar")
        }
    }
}


fun Game.printDebugState() {
    LOGGER.info("========== ESTADO ATUAL DO JOGO ==========")
    LOGGER.info("Nome do jogo: ${currGameName ?: "(local)"}")
    LOGGER.info("Modo alvo (target): $target")
    LOGGER.info("Contagem de passes: $countPass")

    val state = gameState
    if (state == null) {
        LOGGER.info("⚠️ Sem estado de jogo carregado.")
        LOGGER.info("==========================================")
        return
    }

    LOGGER.info("\n--- Jogadores ---")
    state.players.forEachIndexed { i, player ->
        LOGGER.info("Jogador ${i + 1}: ${player.type} (${player.points} pontos)")
    }

    LOGGER.info("Último jogador: ${state.lastPlayer}")
    LOGGER.info("Vencedor: ${state.winner?.type ?: "Nenhum"}")

    val board = state.board
    LOGGER.info("\n--- Tabuleiro ---")
    LOGGER.info("Tamanho: ${board.side}x${board.side}")
    LOGGER.info("Peças pretas: ${board.totalBlackPieces}, Peças brancas: ${board.totalWhitePieces}")
    LOGGER.info("Representação:")
    LOGGER.info(this.stringifyBoard())

    LOGGER.info("==========================================\n")
}

