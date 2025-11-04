package pt.isel.reversi.core

import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.exceptions.EndGameException
import pt.isel.reversi.core.exceptions.InvalidFileException
import pt.isel.reversi.core.exceptions.InvalidGameException
import pt.isel.reversi.core.exceptions.InvalidPlayException
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.storage.Storage

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
 * @property target Indicates if the game is in target mode.
 * @property currGameName The name of the current game for storage purposes.
 * @property gameState The current state of the game, including the board and players.
 * @property countPass The number of consecutive passes made by players.
 */
data class Game(
    val target: Boolean,
    val currGameName: String?,
    val gameState: GameState?,
    val countPass: Int = 0,
) {
    private val storage: Storage<String, GameState, String> = STORAGE

    constructor() : this(
        target = false,
        currGameName = null,
        gameState = null,
    )

    /**
     * Ensures that the game has started by checking if the game state and players are initialized.
     * @return The current game state if the game has started.
     * @throws InvalidGameException if the game is not started yet (game state is null or players list is empty).
     */
    private fun requireStartedGame(): GameState {
        if (gameState == null || gameState.players.isEmpty())
            throw InvalidGameException(
                message = "Game is not started yet."
            )
        return gameState
    }

    /**
     * Checks if it's the player's turn in a not local game.
     * @param gs The game state to check.
     * @throws InvalidPlayException if it's not the player's turn.
     */
    private fun checkTurnOnNotLocalGame(gs: GameState) {
        if (gs.players.size == 1 && gs.players[0].type != gs.lastPlayer.swap()) {
            throw InvalidPlayException("It's not your turn")
        }
    }

    private fun gameEnded() {
        val gs = requireStartedGame()
        if (gs.winner != null) {
            throw EndGameException(
                "The game has already ended. The winner is ${gs.winner.type.symbol} with ${gs.winner.points} points."
            )
        }
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
    fun play(coordinate: Coordinate): Game {
        val gs = requireStartedGame()
        gameEnded()

        checkTurnOnNotLocalGame(gs)

        val piece = Piece(coordinate, gs.lastPlayer.swap())

        val newBoard = GameLogic.play(gs.board, myPiece = piece)

        val refreshPlayers = gs.players.map { it.refresh(newBoard) }

        val newGameState = GameState(
            lastPlayer = piece.value,
            board = newBoard,
            players = refreshPlayers
        )

        if (currGameName != null) {
            saveOnlyPlay(newGameState)
        }

        return this.copy(
            gameState = newGameState,
            countPass = 0
        )
    }

    /**
     * Sets the target mode for the game.
     * @param target True to enable target mode.
     * @return The updated game state.
     */
    fun setTargetMode(target: Boolean): Game =
        this.copy(target = target)

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
            board = gs.board,
            myPieceType = gs.lastPlayer.swap()
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
    fun pass(): Game {
        var gs = requireStartedGame()
        gameEnded()

        checkTurnOnNotLocalGame(gs)

        if (GameLogic.getAvailablePlays(board = gs.board, myPieceType = gs.lastPlayer.swap()).isNotEmpty())
            throw InvalidPlayException("There are available plays, cannot pass the turn")

        if (countPass >= 1) {
            throw EndGameException(
                message = "Both players have passed consecutively. The game has ended."
            )
        }

        gs = gs.copy(
            lastPlayer = gs.lastPlayer.swap(),
        )

        if (currGameName != null) {
            saveOnlyPlay(gs)
        }

        return this.copy(
            gameState = gs,
            countPass = countPass + 1
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
    fun refresh(): Game {
        val gs = requireStartedGame()

        if (currGameName == null) return this

        val loadedState = storage.load(currGameName) ?: throw InvalidFileException(
            message = "Failed to load game state from storage: $currGameName"
        )

        return this.copy(
            gameState = loadedState.copy(players = gs.players.map { it.refresh(loadedState.board) }),
            countPass =
                if (loadedState.board == gs.board
                    && loadedState.lastPlayer != gs.lastPlayer
                )
                    countPass + 1
                else 0
        )
    }

    /**
     * Saves the current game state to storage.
     * Saves the player in storage if not already present (makes available this player for future loads).
     * It is recommended to use this method only to save the game at the end.
     * Only applicable for not local games (players size must be 1).
     * @throws InvalidGameException if the game is local or not started yet.
     * @throws InvalidFileException if the current game name is null.
     */
    fun saveGame() {
        val gs = requireStartedGame()

        if (currGameName == null)
            throw InvalidFileException("Name of the current game is null")

        var playersInStorage = storage.load(currGameName)?.players ?: emptyList()

        gs.players.forEach {
            if (it !in playersInStorage) {
                playersInStorage = playersInStorage + it
            }
        }

        try {
            storage.save(
                id = currGameName,
                obj = gs.copy(
                    players = playersInStorage
                )
            )
        } catch (_: IllegalArgumentException) {
            storage.new(
                id = currGameName,
            ) {
                gs.copy(
                    players = playersInStorage
                )
            }
        }
    }

    /**
     * Saves only the play-related state (board and last player) to storage.
     * Keeps the existing players in storage unchanged.
     * It is recommended to use this method during gameplay to save progress.
     * Only applicable for not local games (players size must be 1).
     * @param gs The current game state to save.
     * @throws InvalidGameException if the game is local or not started yet.
     * @throws InvalidFileException if the current game name is null or loading fails.
     */
    fun saveOnlyPlay(gs: GameState) {

        if (gs.players.size != 1)
            throw InvalidGameException(
                message = "Only a not local game can be saved (players size must be 1)"
            )

        if (currGameName == null)
            throw InvalidFileException("Name of the current game is null")

        val ls = storage.load(currGameName) ?: throw InvalidFileException(
            message = "Failed to load game state from storage: $currGameName"
        )

        storage.save(
            id = currGameName,
            obj = gs.copy(
                players = ls.players
            )
        )
    }
}