package pt.isel.reversi.app.corroutines

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.*
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.utils.LOGGER

/**
 * Launches a coroutine that periodically executes game.refresh at the specified [refreshIntervalMs].
 *
 * @param refreshIntervalMs The interval in milliseconds between each execution.
 * @return A [Job] representing the launched coroutine.
 */
fun CoroutineScope.launchGameRefreshCoroutine(
    refreshIntervalMs: Long,
    appState: MutableState<AppState>,
) {
    this.launch(Dispatchers.Default) {
        while (isActive) {
            try {
                val game = appState.value.game
                if (game.gameState != null && game.currGameName != null) {
                    val newGame = game.refresh()
                    val needsUpdate = newGame.gameState != game.gameState
                    if (needsUpdate)
                        appState.value = appState.value.copy(game = newGame)
                }
            } catch (e: Exception) {
                LOGGER.warning("Auto-refreshing game state gave an error: ${e.message}")
            }
            delay(refreshIntervalMs)
        }
    }
}