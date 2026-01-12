package pt.isel.reversi.core

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.*
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.GameStorageType.Companion.setUpStorage
import pt.isel.reversi.core.storage.MatchPlayers
import pt.isel.reversi.storage.AsyncStorage
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.TRACKER

/**
 * Represents a Reversi game, managing the game state, player turns, and interactions with storage.
 * The game has two modes: local and not local.
 *
 * #### Local Game
 * In a local game, both players are managed within the same game instance. No storage operations are performed.
 *
 * #### Not Local Game
 * In a not local game, only one player is managed within the game instance. The game state is saved and loaded
 *
 * TODO: Test my piece and parameters
 *
 * @property target Indicates if the game is in target mode.
 * @property currGameName The name of the current game for storage purposes.
 * @property gameState The current state of the game, including the board and players.
 * @property countPass The number of consecutive passes made by players.
 */
data class Game(
    val target: Boolean = false,
    val currGameName: String? = null,
    val lastModified: Long? = null,
    val gameState: GameState? = null,
    val countPass: Int = 0,
    val myPiece: PieceType? = null,
    val config: CoreConfig = loadCoreConfig(),
) {
    // make this a lazy property to avoid initializing storage if not needed
    val storage: AsyncStorage<String, GameState, String> by lazy {
        setUpStorage(config)
    }

    init {
        TRACKER.trackClassCreated(this)
    }

    /**
     * Reloads the core configuration.
     * @return A new Game instance with the updated configuration.
     */
    fun reloadConfig(): Game = this.copy(
        config = loadCoreConfig()
    )

    /**
     * Changes the player's piece type.
     * @param newType The new piece type for the player.
     * @return The updated game state with the new piece type.
     */
    fun changeMyPiece(newType: PieceType): Game = this.copy(myPiece = newType)

    /**
     * Ensures that the game has started by checking if the game state and players are initialized.
     * @return The current game state if the game has started.
     * @throws InvalidGameException if the game is not started yet (game state is null or players list is empty).
     */
    private fun requireStartedGame(): GameState {
        if (gameState == null || gameState.players.isEmpty() || myPiece == null) throw InvalidGameException(
            message = "Game is not started yet.", type = ErrorType.INFO
        )
        return gameState
    }

    /**
     * Checks if it's the player's turn in a not local game.
     * @param gs The game state to check.
     * @throws InvalidPlayException if it's not the player's turn.
     */
    private fun checkTurnOnNotLocalGame(gs: GameState) {
        if (currGameName != null && myPiece != gs.lastPlayer.swap()) {
            throw InvalidPlayException(
                message = "It's not your turn", type = ErrorType.INFO
            )
        }
    }

    private fun gameEnded() {
        val gs = requireStartedGame()
        if (gs.winner != null) {
            throw EndGameException(
                message = "The game has already ended. The winner is ${gs.winner.type.symbol} with ${gs.winner.points} points.",
                type = ErrorType.INFO
            )
        }
    }

    private suspend fun hasAllPlayers(): Boolean {
        val gs = requireStartedGame()
        val name = currGameName ?: return (gs.players.isFull())

        val loaded = storage.load(name) ?: throw InvalidFileException(
            message = "Failed to load game state from storage: $name", type = ErrorType.WARNING
        )
        return (loaded.players.isFull())
    }

    /**
     * Plays a move at the specified coordinate.
     * Saves the piece to data access if the game is not local.
     * Only check player turn if it is a not local game.
     * And resets the pass count to 0.
     * @param coordinate The (row, column) coordinate for the move.
     * @return The new game state after the move.
     * @throws InvalidPlayException if it's not the player's turn or if the play is invalid.
     * @throws IllegalArgumentException if the position is out of bounds.
     * @throws InvalidGameException if the game is not started yet (board or players are null,empty).
     * @throws InvalidFileException if there is an error saving the game state.
     * @throws EndGameException if the game has already ended.
     */
    suspend fun play(coordinate: Coordinate): Game {
        val gs = requireStartedGame()
        gameEnded()
        if (!hasAllPlayers()) {
            throw InvalidPlayException(
                message = "Cannot play until all players have joined the game.", type = ErrorType.INFO
            )
        }

        checkTurnOnNotLocalGame(gs)

        val piece = Piece(coordinate, gs.lastPlayer.swap())

        val newBoard = GameLogic.play(gs.board, myPiece = piece)

        val refreshPlayers = gs.players.refreshPlayers(newBoard)

        val newGameState = GameState(
            lastPlayer = piece.value,
            board = newBoard,
            players = refreshPlayers,
            winner = gs.winner
        )

        if (currGameName != null) {
            saveOnlyBoard(newGameState)
        }

        return this.copy(
            gameState = newGameState, countPass = 0
        )
    }

    /**
     * Sets the target mode for the game.
     * @param target True to enable target mode.
     * @return The updated game state.
     */
    fun setTargetMode(target: Boolean): Game = this.copy(target = target)

    /** Gets the available plays for the current player.
     * If it is not a local game, and it is not the player's turn, returns an empty list.
     * @return List of available plays.
     * @throws InvalidGameException if the game is not started yet (board is null).
     */
    fun getAvailablePlays(): List<Coordinate> {
        val gs = requireStartedGame()

        try {
            checkTurnOnNotLocalGame(gs)
        } catch (_: Exception) {
            return emptyList()
        }

        return GameLogic.getAvailablePlays(
            board = gs.board, myPieceType = gs.lastPlayer.swap()
        )
    }

    /**
     * Passes the turn to the next player.
     * Saves the game state if the game is not local.
     * Only check player turn if it is a not local game.
     * @return The new game state after passing the turn.
     * @throws EndGameException if both players have passed consecutively, ending the game.
     * @throws InvalidGameException if the game is not started yet (board or players are null,empty).
     * @throws InvalidFileException if there is an error saving the game state.
     * @throws InvalidPlayException if there are available plays and passing is not allowed.
     * @throws EndGameException if the game has already ended.
     */
    suspend fun pass(): Game {
        var gs = requireStartedGame()
        gameEnded()

        checkTurnOnNotLocalGame(gs)

        if (GameLogic.getAvailablePlays(board = gs.board, myPieceType = gs.lastPlayer.swap())
                .isNotEmpty()
        ) throw InvalidPlayException(
            message = "There are available plays, cannot pass the turn", type = ErrorType.INFO
        )

        if (countPass >= 1) {
            gs = gs.copy(
                winner = when {
                    gs.board.totalBlackPieces > gs.board.totalWhitePieces -> Player(
                        PieceType.BLACK,
                        points = gs.board.totalBlackPieces
                    )

                    gs.board.totalWhitePieces > gs.board.totalBlackPieces -> Player(
                        PieceType.WHITE,
                        points = gs.board.totalWhitePieces
                    )

                    else -> throw EndGameException(
                        message = "The game has ended in a draw.", type = ErrorType.INFO
                    )
                }
            )
        }

        gs = gs.copy(
            lastPlayer = gs.lastPlayer.swap(),
        )

        if (currGameName != null) {
            saveOnlyBoard(gs)
        }

        return this.copy(
            gameState = gs, countPass = countPass + 1
        )
    }

    /**
     * Refreshes the game state from storage (board and last player).
     * Updates players to reflect the current board state.
     * Increments the pass count if the board is unchanged but the last player has changed.
     * @return The refreshed game state.
     * @throws InvalidGameException if the game is not started yet (board or players are null,empty).
     * @throws InvalidFileException if there is an error loading the game state from storage.
     */
    suspend fun refresh(): Game {
        val gs = requireStartedGame()
        if (currGameName == null) return this

        val newLastModified = storage.lastModified(currGameName)

        if (newLastModified == this.lastModified) return this

        val loadedState = refreshBase() ?: return this

        return copy(
            gameState = loadedState.refreshPlayers(),
            countPass = if (loadedState.board == gs.board && loadedState.lastPlayer != gs.lastPlayer) countPass + 1
            else 0,
            lastModified = newLastModified
        )
    }

    suspend fun refreshBase(): GameState? {
        if (currGameName == null) return null

        val lastModified = storage.lastModified(currGameName)

        if (lastModified == this.lastModified) return null

        return storage.load(currGameName) ?: throw InvalidFileException(
            message = "Failed to load game state from storage: $currGameName", type = ErrorType.WARNING
        )
    }

    suspend fun hardRefresh(): Game {
        val gs = refreshBase()
        return if (gs != null) {
            copy(gameState = gs)
        } else this
    }

    /**
     * Saves the current game state to storage.
     * Saves the player in storage if not already present (makes available this player for future loads).
     * It is recommended to use this method only to save the game at the end.
     * Only applicable for not local games (players size must be 1).
     * @throws InvalidGameException if the game is local or not started yet.
     * @throws InvalidFileException if the current game name is null.
     */
    suspend fun saveEndGame() {
        val gs = requireStartedGame()

        val name = currGameName ?: throw InvalidFileException(
            message = "Name of the current game is null", type = ErrorType.WARNING
        )

        storage.lastModified(currGameName) ?: run {
            storage.new(
                id = name,
            ) { gs.copy(players = MatchPlayers()) }
            return
        }

        val loadedGs = try {
            storage.load(currGameName)
        } catch (e: InvalidFileException) {
            storage.delete(currGameName)
            LOGGER.warning("Deleted corrupted game from storage: $currGameName due to ${e.message}")
            return
        }


        var playersInStorage = loadedGs?.players ?: MatchPlayers()

        if (loadedGs != null && loadedGs.winner != null && loadedGs.winner == gs.winner) {
            LOGGER.info("Game already ended in storage: $currGameName")
            storage.delete(currGameName)
            LOGGER.info("Deleted ended game from storage: $currGameName")
            return
        }

        val myPieceTemp = myPiece ?: throw InvalidGameException(
            message = "Game is not started yet.", type = ErrorType.WARNING
        )

        playersInStorage = MatchPlayers(null, playersInStorage.getPlayerByType(myPieceTemp.swap()))

        LOGGER.info("Saving game state to storage: $currGameName")
        storage.save(
            id = currGameName,
            obj = gs.copy(
                players = playersInStorage,
            )
        )
    }

    /**
     * Saves only the play-related state (board and last player) to storage.
     * Keeps the existing players in storage unchanged.
     * It is recommended to use this method during gameplay to save progress.
     * Only applicable for not local games (players size must be 1).
     * @param gameState The current game state to save.
     * @throws InvalidGameException if the game is local or not started yet.
     * @throws InvalidFileException if the current game name is null or loading fails.
     */
    suspend fun saveOnlyBoard(gameState: GameState?) {
        val gs = gameState ?: throw InvalidGameException(
            message = "Game is not started yet.", type = ErrorType.WARNING
        )

        val name = currGameName ?: throw InvalidFileException(
            message = "Name of the current game is null", type = ErrorType.WARNING
        )

        storage.lastModified(id = name) ?: run {
            try {
                storage.new(id = name) { gameState }
                return@saveOnlyBoard
            } catch (e: Exception) {
                throw InvalidFileException(
                    message = e.message.toString(), type = ErrorType.CRITICAL
                )
            }
        }

        val ls = storage.load(id = name) ?: throw InvalidFileException(
            message = "Failed to load game state from storage: $name", type = ErrorType.ERROR
        )


        var lsGameState = ls

        ls.players.forEachIndexed { index, player ->
            val gsPlayer = gs.players[index]
            if (gsPlayer != null && gsPlayer.name != player.name) {
                lsGameState = lsGameState.changeName(newName = gsPlayer.name, pieceType = gsPlayer.type)
            }
        }

        storage.save(
            id = name,
            obj = gs.copy(
                players = lsGameState.players,
            )
        )
    }

    /**
     * Runs a health check on the storage system.
     * Does the full cycle of creating, saving, loading, and deleting a test game state.
     * @return True if the storage is healthy, false otherwise.
     */
    suspend fun runStorageHealthCheck(): Boolean {
        val testId = "health_check_test_game"
        val testState = GameState(
            players = MatchPlayers(Player(PieceType.BLACK)),
            lastPlayer = PieceType.WHITE,
            board = Board(8),
            winner = null
        )
        if (storage.lastModified(testId) != null) storage.delete(testId)

        storage.new(testId) { testState }
        val loadedState = storage.load(testId)
        if (loadedState != testState) return false
        storage.save(testId, testState)
        val reloadedState = storage.load(testId)
        if (reloadedState != testState) return false
        storage.delete(testId)
        storage.load(testId) == null
        return true
    }

    /**
     * Closes the storage connection.
     */
    suspend fun closeStorage() {
        storage.close()
    }
}