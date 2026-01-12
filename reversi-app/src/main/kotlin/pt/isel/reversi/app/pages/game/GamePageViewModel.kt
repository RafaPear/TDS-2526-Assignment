package pt.isel.reversi.app.pages.game

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import pt.isel.reversi.app.exceptions.GameCorrupted
import pt.isel.reversi.app.exceptions.GameNotStartedYet
import pt.isel.reversi.app.state.ScreenState
import pt.isel.reversi.app.state.UiState
import pt.isel.reversi.app.state.ViewModel
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.TRACKER
import kotlin.coroutines.cancellation.CancellationException


data class GameUiState(
    val game: Game,
    override val screenState: ScreenState = ScreenState()
) : UiState() {
    override fun updateScreenState(newScreenState: ScreenState): GameUiState {
        return copy(screenState = newScreenState)
    }
}

/**
 * View model for the game page managing game state, UI updates, and user interactions.
 * Handles game move execution, state polling for multiplayer games, and sound effects.
 *
 * @property appState Global application state containing game and UI configuration.
 * @property scope Coroutine scope for launching async game operations.
 * @property globalError Optional error to display on initial load.
 */
class GamePageViewModel(
    private val game: Game,
    private val scope: CoroutineScope,
    private val setGame: (Game) -> Unit,
    private val audioPlayMove: () -> Unit,
    globalError: ReversiException? = null,
): ViewModel {
    private val _uiState = mutableStateOf(
        GameUiState(
            game = game,
            screenState = ScreenState(error = globalError)
        )
    )
    override val uiState: State<GameUiState> = _uiState

    private var pollingJob: Job? = null

    init {
        TRACKER.trackViewModelCreated(this)
    }

    fun save() {
        if (uiState.value == game) return
        setGame(uiState.value.game)
    }

    override fun setError(error: Exception?) {
        _uiState.setError(error)
    }

    fun startPolling() {
        if (pollingJob != null) throw IllegalStateException("Polling already started")

        LOGGER.info("Starting auto-refreshing game state coroutine")

        scope.launch {
            try {
                while (isActive) {
                    val game = uiState.value.game
                    val gameState = game.gameState
                    val myPiece = game.myPiece ?: run {
                        _uiState.setError(GameNotStartedYet(), ErrorType.ERROR)
                        return@launch
                    }

                    if (game.gameState != null && game.currGameName != null) {
                        var newGame = game.refresh()
                        val newGameState = newGame.gameState ?: run {
                            _uiState.setError(GameCorrupted(), ErrorType.ERROR)
                            return@launch
                        }
                        val myName = gameState?.players?.getPlayerByType(myPiece)?.name

                        if (myName != null) {
                            newGame = newGame.copy(
                                gameState = newGameState.changeName(myName, myPiece)
                            )
                        }

                        val needsUpdate = newGame.lastModified != game.lastModified
                        if (needsUpdate)
                            _uiState.value = _uiState.value.copy(game = newGame)
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
        _uiState.value = uiState.value.copy(game = uiState.value.game.setTargetMode(target))
    }

    fun playMove(coordinate: Coordinate) {
        scope.launch {
            try {
                _uiState.value = uiState.value.copy(
                    game = uiState.value.game.play(coordinate)
                )
                setGame(_uiState.value.game)
                audioPlayMove()
            } catch (e: Exception) {
                setGame(uiState.value.game)
                _uiState.setError(e)
            }
        }
    }

    fun getAvailablePlays() = uiState.value.game.getAvailablePlays()

    fun pass() {
        scope.launch {
            try {
                _uiState.value = uiState.value.copy(game = uiState.value.game.pass())
            } catch (e: Exception) {
                setGame(uiState.value.game)
                _uiState.setError(e)
            }
        }
    }

}