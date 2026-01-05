package pt.isel.reversi.app.pages.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import pt.isel.reversi.app.PLACE_PIECE_SOUND
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ErrorType.Companion.toReversiException
import pt.isel.reversi.utils.LOGGER
import kotlin.coroutines.cancellation.CancellationException

class GamePageViewModel(val appState: MutableState<AppState>, val scope: CoroutineScope) {
    private val _uiState = mutableStateOf(value = appState.value.game)
    val uiState: State<Game> = _uiState

    private var pollingJob: Job? = null

    fun save() {
        appState.setGame(uiState.value)
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
                            save()
                    }
                    delay(50L)
                }
                throw IllegalStateException("Polling coroutine ended unexpectedly")
            } catch (_: CancellationException) {
                LOGGER.info("Game polling cancelled.")
            } catch (e: Exception) {
                val newE = e.toReversiException(ErrorType.CRITICAL)
                LOGGER.warning("Auto-refreshing game state gave an error ${newE.message}")
            } finally {
                save()
                LOGGER.info("Stop auto-refreshing game state coroutine")
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

    fun playMove(coordinate: Coordinate, save: Boolean = true) {
        scope.launch {
            try {
                _uiState.value = uiState.value.play(coordinate)

                //TODO: Need test this sound playing
                appState.getStateAudioPool().run {
                    stop(PLACE_PIECE_SOUND)
                    play(PLACE_PIECE_SOUND)
                }
            } catch (e: Exception) {
                appState.setError(error = e)
            } finally {
                if (save)
                    save()
            }
        }
    }

    fun getAvailablePlays() = uiState.value.getAvailablePlays()
}