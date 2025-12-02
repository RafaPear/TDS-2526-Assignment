package pt.isel.reversi.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER

@Composable
fun FrameWindowScope.MakeMenuBar(appState: MutableState<AppState>, windowState: WindowState, exitAction: () -> Unit) {
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
                exitAction()
            }
        }

        Menu("View") {
            Item("Toggle Fullscreen") {
                windowState.placement =
                    if (windowState.placement == WindowPlacement.Floating) WindowPlacement.Fullscreen
                    else WindowPlacement.Floating
            }
        }

        Menu("Dev") {
            Item("Mostrar Estado do Jogo") {
                appState.value.game.printDebugState()
            }
            Item("Nullify Game State") {
                appState.value = setGame(
                    appState,
                    Game()
                )
                LOGGER.info("Estado do jogo anulado para fins de teste.")
            }
            Item("Reload Config") {
                try {
                    appState.value = setGame(
                        appState,
                        appState.value.game.reloadConfig()
                    )
                    LOGGER.info("Config recarregada com sucesso.")
                } catch (e: ReversiException) {
                    appState.value = setError(appState, error = e)
                }
            }
            Item("Lobby Screen") {
                appState.value = setPage(appState, Page.LOBBY)
            }
        }

        Menu("Ajuda") {
            Item("Sobre") {
                appState.value = setPage(appState, Page.ABOUT)
            }
        }
    }
}
