package pt.isel.reversi.app.pages.lobby

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import pt.isel.reversi.app.exceptions.GameCorrupted
import pt.isel.reversi.app.exceptions.GameIsFull
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.getAllGameNames
import pt.isel.reversi.core.loadGame
import pt.isel.reversi.core.readGame
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.utils.LOGGER

private const val UI_DELAY_SHORT_MS = 100L
private const val POLL_INTERVAL_MS = 1000L

data class LobbyUiState(
    val games: List<Game> = emptyList(),
    val lobbyState: LobbyState = LobbyState.NONE,
    val selectedGame: Game? = null,
    val canRefresh: Boolean = false,
)

class LobbyViewModel(
    val scope: CoroutineScope,
    val appState: MutableState<AppState>,
) {
    private val _uiState = mutableStateOf(LobbyUiState())
    val uiState: State<LobbyUiState> = _uiState

    private var knownNames: List<String> = emptyList()

    private var pollingJob: Job? = null

    fun refreshAll() {
        scope.launch {
            loadGamesAndUpdateState()
        }
    }

    private suspend fun loadGamesAndUpdateState() {
        appState.setLoading(true)

        try {
            val ids = getAllGameNames()
            delay(UI_DELAY_SHORT_MS)
            val loaded = ids.mapNotNull { id ->
                try {
                    readGame(id)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    LOGGER.warning("Erro ao ler jogo: $id - ${e.message}")
                    null
                }
            }
            knownNames = ids
            LOGGER.info("Jogos carregados: ${loaded.map { it.currGameName }}")
            val newLobbyState = if (loaded.isEmpty()) LobbyState.EMPTY else LobbyState.SHOW_GAMES
            appState.setLoading(false)
            _uiState.value = _uiState.value.copy(
                games = loaded,
                lobbyState = newLobbyState,
                canRefresh = false,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LOGGER.warning("Erro ao carregar jogos: ${e.message}")

            _uiState.value = _uiState.value.copy(
                games = emptyList(),
                lobbyState = LobbyState.EMPTY,
                canRefresh = false,
            )
            appState.setLoading(false)
            appState.setError(e)
        }
    }

    fun startPolling() {
        if (pollingJob != null) throw IllegalStateException("Polling already started")

        LOGGER.info("Starting lobby polling.")

        scope.launch {
            try {
                loadGamesAndUpdateState()
                while (isActive) {
                    pollLobbyUpdates()
                }
                throw IllegalStateException("Polling coroutine ended unexpectedly")
            } catch (_: CancellationException) {
                LOGGER.info("Lobby polling cancelled.")
            } catch (e: Exception) {
                LOGGER.severe("Error starting lobby polling: ${e.message}")
            } finally {
                LOGGER.info("Lobby polling stopped.")
            }
        }.also { pollingJob = it }
    }

    private suspend fun pollLobbyUpdates() {
        try {
            val ids = getAllGameNames()
            if (ids != knownNames && ids.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(canRefresh = true)
                knownNames = ids
            } else if (ids.isEmpty() && knownNames.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(canRefresh = true)
                knownNames = ids
            }
        } catch (e: Exception) {
            LOGGER.warning("Polling error: ${e.message}")
        }
        delay(POLL_INTERVAL_MS)
    }

    fun stopPolling() {
        pollingJob?.let {
            pollingJob = null
            it.cancel()
        }
    }

    suspend fun tryLoadGame(gameName: String, desiredType: PieceType): Game? {
        return try {
            loadGame(gameName = gameName, desiredType = desiredType)
        } catch (e: Exception) {
            LOGGER.warning("Erro ao carregar jogo $gameName: ${e.message}")
            appState.setError(e)
            null
        }
    }

    fun refreshGame(game: Game) {
        scope.launch {
            try {
                val newGame = game.hardRefresh()
                if (newGame != game) _uiState.value = _uiState.value.copy(
                    games = _uiState.value.games.map {
                        if (it.currGameName == newGame.currGameName) newGame
                        else it
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LOGGER.warning("Erro: ${e.message}, ${e.localizedMessage}")
            }
        }
    }

    fun selectGame(game: Game?) {
        LOGGER.info("Jogo selecionado: ${game?.currGameName}")
        _uiState.value = _uiState.value.copy(selectedGame = game)
    }

    fun joinGameValidations(game: Game): GameState? {
        val state: GameState? = game.gameState
        val name: String? = game.currGameName

        when {
            name == appState.value.game.currGameName -> {
                appState.setPage(Page.GAME)
                appState.value = appState.value.copy(backPage = Page.LOBBY)
                return state
            }

            state == null -> {
                LOGGER.warning("Estado do jogo nulo para o jogo: $name")
                appState.setError(
                    GameCorrupted(
                        message = "O jogo '${name}' está corrompido.",
                        type = ErrorType.ERROR
                    )
                )
                return null
            }

            state.players.isEmpty() -> {
                LOGGER.warning("Jogo cheio selecionado: $name")
                appState.setError(GameIsFull())
                return null
            }

            name == null -> {
                LOGGER.warning("Nome do jogo nulo ao tentar entrar no jogo.")
                appState.setError(
                    GameCorrupted(
                        message = "O jogo selecionado não tem um nome válido.",
                        type = ErrorType.ERROR
                    )
                )
                return null
            }
        }
        return state
    }

    fun joinGame(game: Game, pieceType: PieceType) {
        val appGame = appState.value.game
        val name = game.currGameName ?: return

        scope.launch {
            try {
                appState.setLoading(true)

                try {
                    appGame.saveEndGame()
                } catch (e: Exception) {
                    LOGGER.warning("Erro ao salvar estado atual do jogo: ${e.message}")
                }

                val joinedGame = tryLoadGame(gameName = name, desiredType = pieceType) ?: run {
                    selectGame(null)
                    return@launch
                }

                LOGGER.info("Entrou no jogo '${joinedGame.currGameName}' como peça $pieceType.")

                appState.setAppState(joinedGame, Page.GAME, backPage = Page.LOBBY)
                selectGame(null)
            } finally {
                appState.setLoading(false)
            }
        }
    }
}
