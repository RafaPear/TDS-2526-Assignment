package pt.isel.reversi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import pt.isel.reversi.app.exceptions.GameNotStartedYet
import pt.isel.reversi.app.pages.MainMenu
import pt.isel.reversi.app.pages.NewGamePage
import pt.isel.reversi.app.pages.SettingsPage
import pt.isel.reversi.app.pages.game.GamePage
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyMenu
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.stringifyBoard
import pt.isel.reversi.utils.LOGGER
import reversi.reversi_app.generated.resources.Res
import reversi.reversi_app.generated.resources.reversi
import java.lang.System.setProperty

/**
 * Entry point for the desktop Reversi application. Initializes app dependencies
 * and launches the Compose window with the current `AppState`.
 *
 * @param args Optional command-line arguments forwarded to initialization.
 */
fun main(args: Array<String>) {
    val initializedArgs = initializeAppArgs(args) ?: return
    val (audioPool) = initializedArgs
    setProperty("apple.awt.application.name", "Reversi-DEV")

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
                    audioPool = audioPool,
                    theme = AppThemes.DARK.appTheme,
                )
            )
        }

        fun safeExitApplication() {
            LOGGER.info("Exiting application...")

            try {
                runBlocking { appState.value.game.saveEndGame() }
                appState.getStateAudioPool().destroy()
            } catch (e: Exception) {
                LOGGER.warning("Failed to save game on exit: ${e.message}")
            }

            exitApplication()
        }

        Window(
            onCloseRequest = ::safeExitApplication,
            title = "Reversi-DEV",
            icon = painterResource(Res.drawable.reversi),
            state = windowState,
        ) {
            val scope = rememberCoroutineScope()

            window.minimumSize = java.awt.Dimension(800, 800)

            MakeMenuBar(appState, windowState, ::safeExitApplication)
            AppScreenSwitcher(appState) { page ->
                LOGGER.info("Navigating to page: $page")
                when (page) {
                    Page.MAIN_MENU -> MainMenu(appState)
                    Page.GAME -> GamePage(GamePageViewModel(appState, scope))
                    Page.SETTINGS -> SettingsPage(appState)
                    Page.ABOUT -> AboutPage(appState)
                    Page.NEW_GAME -> NewGamePage(appState)
                    Page.SAVE_GAME -> SaveGamePage(appState)
                    Page.LOBBY -> LobbyMenu(LobbyViewModel(scope, appState))
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
fun SaveGamePage(appState: MutableState<AppState>) {
    val game = appState.value.game
    if (game.gameState == null) {
        appState.setAppState(
            page = appState.value.backPage,
            error = GameNotStartedYet(
                message = "Not possible to save a game that has not started yet",
                type = ErrorType.WARNING
            )
        )
        return
    }

    var gameName by remember { mutableStateOf(game.currGameName) }
    val coroutineAppScope = rememberCoroutineScope()


    ScaffoldView(
        appState = appState,
        title = "Guardar Jogo",
        previousPageContent = {
            PreviousPage { appState.setPage(Page.GAME) }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(paddingValues = padding),
        ) {
            Column(
                modifier = Modifier.background(Color.Transparent).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(height = 24.dp))

                ReversiTextField(
                    value = gameName ?: "",
                    enabled = appState.value.game.currGameName == null,
                    onValueChange = { gameName = it },
                    label = { ReversiText("Nome do jogo", color = getTheme().textColor,) },
                    singleLine = true
                )

                Spacer(Modifier.height(height = 24.dp))

                Button(
                    onClick = {
                        appState.setGame(game.copy(currGameName = gameName?.trim() ?: return@Button))
                        coroutineAppScope.launch {
                            try {
                                appState.value.game.saveOnlyBoard(gameState = appState.value.game.gameState)
                                appState.setPage(Page.GAME)
                            } catch (e: Exception) {
                                appState.setAppState(
                                    error = e,
                                    game = game.copy(currGameName = null)
                                )
                            }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = getTheme().primaryColor
                    )
                ) {
                    ReversiText("Guardar", color = getTheme().primaryColor,)
                }
            }
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

/**
 * Simple about page presenting project and authorship information.
 *
 * @param appState Global state holder used for navigation and theming.
 * @param modifier Optional modifier to adjust layout in previews or reuse.
 */
@Composable
fun AboutPage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {

    ScaffoldView(
        appState = appState,
        title = "Sobre",
        previousPageContent = {
            PreviousPage { appState.setPage(appState.value.backPage) }
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues = padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(height = 24.dp))
            ReversiText("Projeto Reversi desenvolvido no ISEL.", color = getTheme().textColor,)
            ReversiText("Autores: ", color = getTheme().textColor,)
            ReversiText(" - Rafael Pereira - NUMERO", color = getTheme().textColor,)
            ReversiText(" - Ian Frunze - NUMERO", color = getTheme().textColor,)
            ReversiText(" - Tito Silva - NUMERO", color = getTheme().textColor,)
            ReversiText("Versão: DEV Build", color = getTheme().textColor,)

        }
    }
}

/**
 * Logs the current game state for debugging, including players, scores, and board layout.
 */
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
