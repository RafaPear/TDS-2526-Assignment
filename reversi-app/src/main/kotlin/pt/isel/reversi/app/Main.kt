package pt.isel.reversi.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource
import pt.isel.reversi.app.pages.aboutPage.AboutPage
import pt.isel.reversi.app.pages.aboutPage.AboutPageViewModel
import pt.isel.reversi.app.pages.game.GamePage
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyMenu
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.menu.MainMenu
import pt.isel.reversi.app.pages.menu.MainMenuViewModel
import pt.isel.reversi.app.pages.newGamePage.NewGamePage
import pt.isel.reversi.app.pages.newGamePage.NewGameViewModel
import pt.isel.reversi.app.pages.settingsPage.SettingsPage
import pt.isel.reversi.app.pages.settingsPage.SettingsViewModel
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ErrorType.Companion.toReversiException
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.loadCoreConfig
import pt.isel.reversi.core.stringifyBoard
import pt.isel.reversi.utils.ExportFormat
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.TRACKER
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


    // Initialize tracker with auto-save enabled
    TRACKER.setTrackerFilePath(autoSave = true)
    LOGGER.info("Development tracker initialized with auto-save enabled")

    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.PlatformDefault
        )
        // unica forma que permite sincronizar o estado do app no exit
        val appJob = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + appJob)

        // start storage health check coroutine

        val pageState = remember { mutableStateOf(Page.MAIN_MENU) }
        val backPageState = remember { mutableStateOf(Page.NONE) }
        val themeState = remember { mutableStateOf(AppThemes.DARK.appTheme) }
        val game = remember { mutableStateOf(Game()) }
        val audioPool = remember { mutableStateOf(initializedArgs.audioPool) }
        val playerName = remember { mutableStateOf<String?>(null) }
        val globalError = remember { mutableStateOf<ReversiException?>(null) }

        val appState = AppState(
            game = game.value,
            page = pageState.value,
            backPage = backPageState.value,
            audioPool = audioPool.value,
            theme = themeState.value,
            globalError = globalError.value,
            playerName = playerName.value
        )

        scope.launch {
            //TODO: se for para ter loading precisa estar dentro de alguma pagina
            //setLoading(appState, true)
            val conf = loadCoreConfig()
            val exception = runStorageHealthCheck(testConf = conf, save = true)
            if (exception != null) {
                globalError.value = exception.toReversiException(ErrorType.WARNING)
                LOGGER.severe("Storage type change failed: ${exception.message}")
            }
            //setLoading(appState, false)
        }

        fun safeExitApplication() {
            LOGGER.info("Application exit requested")
            LOGGER.info("Stopping compose application...")

            try {
                LOGGER.info("Saving tracking data...")
                TRACKER.saveToFile()
                LOGGER.info("Tracking data saved.")

                LOGGER.info("Cancelling application coroutines...")
                appJob.cancel()
                pageState.setPage(Page.NONE)
                runBlocking {
                    LOGGER.info("Waiting for application coroutines to finish...")
                    appJob.join()
                    LOGGER.info("Application coroutines finished.")
                    LOGGER.info("Saving game state...")
                    appState.game.saveEndGame()
                    LOGGER.info("Game state saved.")
                    LOGGER.info("Closing game storage...")
                    appState.game.closeStorage()
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
            for (type in ExportFormat.entries) TRACKER.saveToFile(type)

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
            MakeMenuBar(
                appState,
                windowState,
                setPage = { pageState.setPage(it) },
                setGame = { game.setGame(it) },
                setTheme = { themeState.value = it },
                setGlobalError = {
                    globalError.value = it as? ReversiException ?: it?.toReversiException(ErrorType.WARNING)
                },
            ) { safeExitApplication() }

            val lastLoggedPage = remember { mutableStateOf<Page?>(null) }

            val currentPage = pageState.value

            // Create one ViewModel per page change
            val currentViewModel: ViewModel? = remember(currentPage, globalError.value) {
                when (currentPage) {
                    Page.MAIN_MENU -> MainMenuViewModel(scope = scope, globalError = globalError.value)
                    Page.GAME -> GamePageViewModel(
                        game.value,
                        globalError = globalError.value,
                        scope = scope,
                        audioPlayMove = {
                            audioPool.value.run {
                                stop(themeState.value.placePieceSound)
                                play(themeState.value.placePieceSound)
                            }
                        },
                        setGame = { game.setGame(it) },
                    )

                    Page.SETTINGS -> SettingsViewModel(
                        scope,
                        setTheme = { themeState.value = it },
                        setPlayerName = {
                            Snapshot.withMutableSnapshot {
                                playerName.value = it
                                val newName = it ?: return@withMutableSnapshot
                                val gameState = game.value.gameState ?: return@withMutableSnapshot
                                val myPiece = game.value.myPiece ?: return@withMutableSnapshot
                                game.setGame(
                                    game.value.copy(
                                        gameState = gameState.changeName(newName, myPiece)
                                    )
                                )
                            }
                        },
                        audioPool = audioPool.value,
                        globalError = globalError.value
                    )

                    Page.ABOUT -> AboutPageViewModel(scope, globalError.value)
                    Page.NEW_GAME -> NewGameViewModel(scope, globalError.value)
                    Page.LOBBY -> LobbyViewModel(
                        scope = scope,
                        appState = appState,
                        pickGame = {
                            Snapshot.withMutableSnapshot {
                                game.setGame(it)
                                pageState.setPage(Page.GAME)
                            }
                        },
                        globalError = globalError.value,
                    )

                    Page.NONE -> null
                }
            }

            TRACKER.trackRecomposition()

            // Log navigation once per page change
            LaunchedEffect(currentPage) {
                LOGGER.info("Navigating to page: $currentPage")
                lastLoggedPage.value = currentPage
            }

            AppScreenSwitcher(pageState.value, backPageState.value, themeState.value) { currentPage ->
                with(ReversiScope(appState)) {
                    when (currentPage) {
                        Page.MAIN_MENU -> if (currentViewModel is MainMenuViewModel) {
                            MainMenu(
                                viewModel = currentViewModel,
                                setPage = { pageState.setPage(it) },
                                onLeave = {}
                            )
                        }

                        Page.GAME -> if (currentViewModel is GamePageViewModel) {
                            GamePage(
                                viewModel = currentViewModel,
                                onLeave = {
                                    Snapshot.withMutableSnapshot {
                                        game.setGame(it)
                                        pageState.setPage(Page.MAIN_MENU)
                                    }
                                }
                            )
                        }

                        Page.SETTINGS -> if (currentViewModel is SettingsViewModel) {
                            SettingsPage(
                                viewModel = currentViewModel,
                                onLeave = {
                                    pageState.setPage(Page.MAIN_MENU)
                                },
                            )
                        }

                        Page.ABOUT -> if (currentViewModel is AboutPageViewModel) {
                            AboutPage(
                                viewModel = currentViewModel,
                                onLeave = {
                                    pageState.setPage(Page.MAIN_MENU)
                                }
                            )
                        }

                        Page.NEW_GAME -> if (currentViewModel is NewGameViewModel) {
                            NewGamePage(
                                viewModel = currentViewModel,
                                playerNameChange = { name -> playerName.value = name },
                                onLeave = {
                                    pageState.setPage(Page.MAIN_MENU)
                                },
                                createGame = { newGame ->
                                    Snapshot.withMutableSnapshot {
                                        game.setGame(newGame)
                                        pageState.setPage(Page.GAME)
                                    }
                                }
                            )
                        }

                        Page.LOBBY -> if (currentViewModel is LobbyViewModel) {
                            LobbyMenu(
                                currentViewModel,
                                onLeave = { pageState.setPage(Page.MAIN_MENU) }
                            )
                        }

                        Page.NONE -> {}
                    }
                }
            }
        }
    }
}

///**
// * Page to save the current game.
// * Save only board state and last player, not players info
// * (for avoid conflicts, because if save players info, in current game, permit other person to play with same piece type).
// * If the game has no name, allows the user to enter a name.
// * If the game has a name, shows the name but does not allow editing.
// * When the user clicks the save button, saves the game and returns to the game page.
// */
//@Composable
//fun SaveGamePage(game: Game, onSave: () -> Unit, onLeave: () -> Unit, error: (Exception) -> Unit) {
//    if (game.gameState == null) {
//        error(
//            GameNotStartedYet(
//                message = "Not possible to save a game that has not started yet",
//                type = ErrorType.WARNING
//            )
//        )
//        onLeave()
//        return
//    }
//
//    var gameName by remember { mutableStateOf(game.currGameName) }
//    val coroutineAppScope = rememberCoroutineScope()
//
//
//    ScaffoldView(
//        appState = appState,
//        title = "Guardar Jogo",
//        previousPageContent = {
//            PreviousPage { setPage(appState, Page.GAME) }
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier.fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.3f))
//                .padding(paddingValues = padding),
//        ) {
//            Column(
//                modifier = Modifier.background(Color.Transparent).fillMaxSize(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Spacer(Modifier.height(height = 24.dp))
//
//                ReversiTextField(
//                    value = gameName ?: "",
//                    enabled = appState.game.value.currGameName == null,
//                    onValueChange = { gameName = it },
//                    label = { ReversiText("Nome do jogo", color = appState.theme.value.textColor) },
//                    singleLine = true
//                )
//
//                Spacer(Modifier.height(height = 24.dp))
//
//                Button(
//                    onClick = {
//                        val savedGame = game.copy(currGameName = gameName?.trim() ?: return@Button)
//                        coroutineAppScope.launch {
//                            try {
//                                savedGame.saveOnlyBoard(gameState = savedGame.gameState)
//                                setPage(appState, Page.GAME)
//                            } catch (e: Exception) {
//                                setError(appState, e)
//                            }
//                        }
//                    },
//                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
//                        containerColor = appState.theme.value.primaryColor
//                    )
//                ) {
//                    ReversiText("Guardar", color = appState.theme.value.textColor)
//                }
//            }
//        }
//    }
//}


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
