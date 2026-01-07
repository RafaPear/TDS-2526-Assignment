package pt.isel.reversi.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.utils.LOGGER

/**
 * Creates the application menu bar with File, View, Dev, and Help menus.
 * Provides navigation to different pages and application controls.
 *
 * @param appState Global application state for navigation and configuration.
 * @param windowState Window state for toggling fullscreen mode.
 * @param exitAction Callback function to execute on application exit.
 */
@Composable
fun FrameWindowScope.MakeMenuBar(appState: MutableState<AppState>, windowState: WindowState, exitAction: () -> Unit) {
    MenuBar {
        Menu("Ficheiro") {
            Item("Novo Jogo") {
                appState.setPage(Page.NEW_GAME)
            }
            Item("Guardar Jogo") {
                appState.setPage(Page.SAVE_GAME)
            }
            Item("Definições") {
                appState.setPage(Page.SETTINGS)
            }
            Item("Menu Principal") {
                appState.setPage(Page.MAIN_MENU)
            }
            Item("Jogo Atual") {
                appState.setPage(Page.GAME)
            }
            Item("Sair do jogo atual") {
                runBlocking{ appState.value.game.saveEndGame() }
                appState.setGame(Game())
                appState.setPage(Page.MAIN_MENU)
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
                appState.value = appState.value.copy() // Epa foi o q arranjei para forcar o gajo a dar update lol
            }
        }

        Menu("Dev") {
            Item("Mostrar Estado do Jogo") {
                appState.value.game.printDebugState()
            }
            Item("Nullify Game State") {
                appState.setGame(
                    Game()
                )
                LOGGER.info("Estado do jogo anulado para fins de teste.")
            }
            Item("Reload Config") {
                try {
                    appState.setGame(
                        appState.value.game.reloadConfig()
                    )
                    LOGGER.info("Config recarregada com sucesso.")
                } catch (e: Exception) {
                    appState.setError(error = e)
                }
            }
            Item("Lobby Screen") {
                appState.setPage(Page.LOBBY)
            }
        }

        Menu("Ajuda") {
            Item("Sobre") {
                appState.setPage(Page.ABOUT)
            }
        }
    }
}
