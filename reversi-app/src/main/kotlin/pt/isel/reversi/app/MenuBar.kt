package pt.isel.reversi.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.core.Game
import pt.isel.reversi.utils.LOGGER

/**
 * Creates the application menu bar with File, View, Dev, and Help menus.
 * Provides navigation to different pages and application controls.
 *
 * @param appState Global application state for navigation and configuration.
 * @param windowState Window state for toggling fullscreen mode.
 * @param setPage Callback to change the current page.
 * @param setGame Callback to update the current game instance.
 * @param setTheme Callback to reapply the current theme (used after fullscreen toggle).
 * @param setGlobalError Callback to surface errors triggered from menu actions.
 * @param exitAction Callback function to execute on application exit.
 */
@Composable
fun FrameWindowScope.MakeMenuBar(
    appState: AppState,
    windowState: WindowState,
    setPage: (Page) -> Unit,
    setGame: (Game) -> Unit,
    setTheme: (AppTheme) -> Unit,
    setGlobalError: (Exception?) -> Unit,
    exitAction: () -> Unit
) {
    MenuBar {
        Menu("Ficheiro") {
            Item("Novo Jogo") {
                setPage(Page.NEW_GAME)
            }
//            Item("Guardar Jogo") {
//                setPage(Page.SAVE_GAME)
//            }
            Item("Definições") {
                setPage(Page.SETTINGS)
            }
            Item("Menu Principal") {
                setPage(Page.MAIN_MENU)
            }
            Item("Jogo Atual") {
                try {
                    appState.game.requireStartedGame()
                    setPage(Page.GAME)
                } catch (e: Exception) {
                    setGlobalError(e)
                    return@Item
                }
            }
            Item("Sair do jogo atual") {
                runBlocking { appState.game.saveEndGame() }
                setGame(Game())
                setPage(Page.MAIN_MENU)
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
                setTheme(appState.theme) // Force recomposition
            }
        }

        Menu("Dev") {
            Item("Mostrar Estado do Jogo") {
                // Use the extension function defined in the pt.isel.reversi.app package
                appState.game.printDebugState()
            }
            Item("Estatísticas de Tracking") {
                setPage(Page.STATISTICS)
            }
            Item("Nullify Game State") {
                setGame(Game())
                LOGGER.info("Estado do jogo anulado para fins de teste.")
            }
            Item("Reload Config") {
                try {
                    setGame(appState.game.reloadConfig())
                    LOGGER.info("Config recarregada com sucesso.")
                } catch (e: Exception) {
                    setGlobalError(e)
                }
            }
            Item("Trigger Error") {
                setGlobalError(Exception("Erro de teste disparado a partir do menu Dev"))
            }
            Item("Crash App") {
                throw RuntimeException("App crash triggered from Dev menu")
            }
        }

        Menu("Ajuda") {
            Item("Sobre") {
                setPage(Page.ABOUT)
            }
        }
    }
}
