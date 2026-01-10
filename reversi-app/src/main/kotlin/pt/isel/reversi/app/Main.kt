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
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource
import pt.isel.reversi.app.exceptions.GameNotStartedYet
import pt.isel.reversi.app.pages.NewGamePage
import pt.isel.reversi.app.pages.SettingsPage
import pt.isel.reversi.app.pages.game.GamePage
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyMenu
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.mainmenu.MainMenu
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
    setProperty("apple.awt.application.name", "Reversi-DEV")
    val initializedArgs = initializeAppArgs(args) ?: return
    val (audioPool) = initializedArgs

    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.PlatformDefault
        )
        // unica forma que permite sincronizar o estado do app no exit
        val appJob = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + appJob)

        // start storage health check coroutine
        scope.launch { runStorageHealthCheck() }

        val appState = remember { AppState.empty() }
        appState.audioPool.merge(audioPool)

        fun safeExitApplication() {
            LOGGER.info("Application exit requested")
            LOGGER.info("Stopping compose application...")

            try {
                LOGGER.info("Cancelling application coroutines...")
                appJob.cancel()
                setPage(appState, Page.NONE) // prevent new operations
                runBlocking {
                    LOGGER.info("Waiting for application coroutines to finish...")
                    appJob.join()
                    LOGGER.info("Application coroutines finished.")
                    LOGGER.info("Saving game state...")
                    appState.game.value.saveEndGame()
                    LOGGER.info("Game state saved.")
                    LOGGER.info("Closing game storage...")
                    appState.game.value.closeStorage()
                    LOGGER.info("Game storage closed.")
                }
                LOGGER.info("Destroying audio pool...")
                getStateAudioPool(appState).destroy()
                LOGGER.info("Audio pool destroyed. Application exited safely.")
            } catch (e: Exception) {
                LOGGER.info("Did it blow up? ${e.message}")
            }
            exitApplication()
        }

        installFatalCrashLogger()
        addShutdownHook {
            LOGGER.info("SHUTDOWN HOOK TRIGGERED")
            for (handler in LOGGER.handlers) {
                handler.flush()
                handler.close()
            }
            safeExitApplication()
        }

        Window(
            onCloseRequest = { safeExitApplication() },
            title = "Reversi-DEV",
            icon = painterResource(Res.drawable.reversi),
            state = windowState,
        ) {
            window.minimumSize = java.awt.Dimension(800, 800)

            MakeMenuBar(appState, windowState) { safeExitApplication() }
            val page = remember { derivedStateOf { appState.page.value } }
            val backPage = remember { derivedStateOf { appState.backPage.value } }
            val theme = remember { derivedStateOf { appState.theme.value } }

            AppScreenSwitcher(page.value, backPage.value, theme.value) { currentPage ->
                LOGGER.info("Navigating to page: $currentPage")
                when (currentPage) {
                    Page.MAIN_MENU -> MainMenu(appState)
                    Page.GAME -> GamePage(GamePageViewModel(appState, scope, { game -> setGame(appState, game) }, { e -> setError(appState, e) }))
                    Page.SETTINGS -> SettingsPage(appState)
                    Page.ABOUT -> AboutPage(appState)
                    Page.NEW_GAME -> NewGamePage(appState)
                    Page.SAVE_GAME -> SaveGamePage(appState)
                    Page.LOBBY -> LobbyMenu(LobbyViewModel(scope, appState))
                    Page.NONE -> { /* No UI to show */ }
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
fun SaveGamePage(appState: AppState) {
    val game = appState.game.value
    if (game.gameState == null) {
        setAppState(
            appState = appState,
            page = appState.backPage.value,
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
            PreviousPage { setPage(appState, Page.GAME) }
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
                    enabled = appState.game.value.currGameName == null,
                    onValueChange = { gameName = it },
                    label = { ReversiText("Nome do jogo", color = appState.theme.value.textColor) },
                    singleLine = true
                )

                Spacer(Modifier.height(height = 24.dp))

                Button(
                    onClick = {
                        val savedGame = game.copy(currGameName = gameName?.trim() ?: return@Button)
                        coroutineAppScope.launch {
                            try {
                                savedGame.saveOnlyBoard(gameState = savedGame.gameState)
                                setPage(appState, Page.GAME)
                            } catch (e: Exception) {
                                setError(appState, e)
                            }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = appState.theme.value.primaryColor
                    )
                ) {
                    ReversiText("Guardar", color = appState.theme.value.primaryColor)
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
fun AboutPage(appState: AppState, modifier: Modifier = Modifier) {

    ScaffoldView(
        appState = appState,
        title = "Sobre",
        previousPageContent = {
            PreviousPage { setPage(appState, appState.backPage.value) }
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues = padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(height = 24.dp))
            ReversiText("Projeto Reversi desenvolvido no ISEL.", color = appState.theme.value.textColor)
            ReversiText("Autores: ", color = appState.theme.value.textColor)
            ReversiText(" - Rafael Pereira - NUMERO", color = appState.theme.value.textColor)
            ReversiText(" - Ian Frunze - NUMERO", color = appState.theme.value.textColor)
            ReversiText(" - Tito Silva - NUMERO", color = appState.theme.value.textColor)
            ReversiText("Versão: DEV Build", color = appState.theme.value.textColor)

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
