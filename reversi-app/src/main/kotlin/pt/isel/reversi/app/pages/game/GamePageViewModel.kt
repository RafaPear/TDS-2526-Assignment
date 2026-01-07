package pt.isel.reversi.app.pages.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setAppState
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.utils.LOGGER
import kotlin.coroutines.cancellation.CancellationException

/**
 * View model for the game page managing game state, UI updates, and user interactions.
 * Handles game move execution, state polling for multiplayer games, and sound effects.
 *
 * @property appState Global application state containing game and UI configuration.
 * @property scope Coroutine scope for launching async game operations.
 */
class GamePageViewModel(val appState: MutableState<AppState>, val scope: CoroutineScope) {
    private val _uiState = mutableStateOf(value = appState.value.game)
    val uiState: State<Game> = _uiState

    private var pollingJob: Job? = null

    init{
        LOGGER.info("GamePageViewModel created: ${this@GamePageViewModel}")
    }

    fun save() {
        appState.setGame(game = uiState.value)
    }

    fun startPolling() {
        if (pollingJob != null) throw IllegalStateException("Polling already started")

        LOGGER.info("Starting auto-refreshing game state coroutine")

        scope.launch {
            try {
                while (isActive) {
                    val game = uiState.value

                    if (game.gameState != null && game.currGameName != null) {
                        val newGame = game.refresh()
                        val needsUpdate = newGame.gameState != game.gameState
                        if (needsUpdate)
                            _uiState.value = newGame
                    }
                    delay(50L)
                }
                throw IllegalStateException("Polling coroutine ended unexpectedly")
            } catch (_: CancellationException) {
                LOGGER.info("Game polling cancelled.")
            } catch (e: Exception) {
                LOGGER.warning("Auto-refreshing game state gave an error ${e.message}")
            } finally {
                LOGGER.info("Stop auto-refreshing game state coroutine: ${this@GamePageViewModel}")
            }
        }.also { pollingJob = it }
    }

    fun stopPolling() {
        pollingJob?.let {
            pollingJob = null
            it.cancel()
        }
    }

    fun isPollingActive() = pollingJob != null

    fun setTarget(target: Boolean) {
        _uiState.value = uiState.value.setTargetMode(target)
    }

    fun playMove(coordinate: Coordinate) {
        scope.launch {
            try {
                _uiState.value = uiState.value.play(coordinate)
                val theme = appState.value.theme

                appState.getStateAudioPool().run {
                    stop(theme.placePieceSound)
                    play(theme.placePieceSound)
                }
            } catch (e: Exception) {
                appState.setAppState(error = e, game = _uiState.value)
            }
        }
    }

    fun getAvailablePlays() = uiState.value.getAvailablePlays()

    fun pass() {
        scope.launch {
            try {
                _uiState.value = uiState.value.pass()
            } catch (e: Exception) {
                appState.setAppState(error = e, game = _uiState.value)
            }
        }
    }

}