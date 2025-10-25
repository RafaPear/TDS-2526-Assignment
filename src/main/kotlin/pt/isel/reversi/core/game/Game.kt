package pt.isel.reversi.core.game

import pt.isel.reversi.core.Environment.firstPlayerTurn
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.exceptions.InvalidGameException
import pt.isel.reversi.core.game.exceptions.InvalidPlayException

/**
 * Lightweight test/dummy implementation of [GameImpl] used for data access and integration tests.
 *
 * Only acts as a structural carrier for required properties; behavioural methods are left as TODOs
 * so they surface if accidentally invoked in logic outside targeted tests. Use the nested helper
 * subclasses to build simple game states for tests (empty, one player or two players).
 *
 * Note: This class is intentionally minimal and not suitable for exercising game logic.
 */
@Suppress("unused")
open class Game(
    override val dataAccess: GDAImpl,
    override val players: List<Player>,
    override val target: Boolean,
    override val playerTurn: PieceType = firstPlayerTurn,
    override val currGameName: String?,
    override val board: Board?
) : GameImpl {
    /**
     * Plays a move at the specified coordinate.
     * Saves the piece to data access if the game is not local.
     * Only check player turn if it is a not local game.
     * @param coordinate The (row, column) coordinate for the move.
     * @return The new game state after the move.
     * @throws InvalidPlayException if it's not the player's turn or if the play is invalid.
     * @throws IllegalArgumentException if the position is out of bounds.
     * @throws InvalidGameException if the game is not started yet (board is null).
     */
    override fun play(coordinate: Coordinate): GameImpl {
        var newBoard = board ?: throw InvalidGameException(
            message = "Game is not started yet (board is null)."
        )

        //if it is not a local game, and it is not the player's turn
        if (players.size == 1 && players[0].type != playerTurn) {
            throw InvalidPlayException(
                message = "It's not your turn"
            )
        }

        val piece = Piece(coordinate, playerTurn)

        newBoard = GameLogic().play(newBoard, myPiece = piece)

        val newPlayers = players.map {
            if (it.type == playerTurn) it.refresh(newBoard) else it
        }
        val nextPlayerTurn = playerTurn.swap()

        // save the piece to the data access if game is not local
        val tempCurrGameName = currGameName

        if (tempCurrGameName != null) {
            dataAccess.postPiece(tempCurrGameName, piece)
        }

        return this.copy(
            board = newBoard,
            players = newPlayers,
            playerTurn = nextPlayerTurn
        )
    }

    /**
     * Gets the available piece options in the data access for the current game.
     * if the game is local (currGameName is null), returns an empty list.
     * @return List of available piece types.
     * @throws java.io.IOException if there is an error accessing the data.
     */
    override fun pieceOptions(): List<PieceType> {
        val tempCurrGameName = currGameName ?: return emptyList()
        return dataAccess.getAvailablePieces(tempCurrGameName)
    }

    /**
     * Sets the target mode for the game.
     * @param target True to enable target mode.
     * @return The updated game state.
     */
    override fun setTargetMode(target: Boolean): GameImpl =
        this.copy(target = target)

    /** Gets the available plays for the current player.
     * If it is not a local game, and it is not the player's turn, returns an empty list.
     * @return List of available plays.
     * @throws InvalidGameException if the game is not started yet (board is null).
     */
    override fun getAvailablePlays(): List<Coordinate> {
        // if it is not a local game, and it is not the player's turn
        if (players.size == 1 && players[0].type != playerTurn) {
            return emptyList()
        }

        return GameLogic().getAvailablePlays(
            board = board ?: throw InvalidGameException(
                message = "Game is not started yet (board is null)."
            ),
            myPieceType = playerTurn
        )
    }

    /**
     * Starts a new game.
     * @param side The side length of the board.
     * @param players The list of players.
     * @param firstTurn The piece type of the player who goes first.
     * @param currGameName The current game name.
     * @return The new game state.
     */
    override fun startNewGame(
        side: Int,
        players: List<Player>,
        firstTurn: PieceType,
        currGameName: String?,
    ): GameImpl {
        val board = Board(side).startPieces()
        return this.copy(
            board = board,
            players = players.map { it.refresh(board) },
            currGameName = currGameName,
            playerTurn = firstTurn
        )
    }

    override fun pass(): GameImpl {
        if (players.size == 1 && players[0].type != playerTurn) {
            throw InvalidPlayException(
                message = "It's not your turn"
            )
        }

        val tempCurrGameName = currGameName

        if (tempCurrGameName != null) {
            dataAccess.postPass(tempCurrGameName, playerTurn)
        }
        return this.copy(
            playerTurn = playerTurn.swap()
        )
    }


    override fun refresh(): GameImpl {
        TODO("Not yet implemented")
    }

    override fun poopBoard(): Board = TODO("Board is public, use the property directly")


    /**
     * Convenience copy function to mutate selected fields for tests.
     */
    override fun copy(
        dataAccess: GDAImpl,
        players: List<Player>,
        target: Boolean,
        playerTurn: PieceType,
        currGameName: String?,
        board: Board?,
    ): GameImpl =
        Game(
            dataAccess = dataAccess,
            players = players,
            currGameName = currGameName,
            board = board,
            target = target,
            playerTurn = playerTurn,
        )

    /**
     * Helper representing a game with no players — useful to test initial write semantics.
     */
    class EmptyPlayers(dataAccess: GDAImpl, path: String) : Game(
        dataAccess = dataAccess,
        players = emptyList(),
        currGameName = path,
        board = Board(8),
        target = false,
        playerTurn = firstPlayerTurn,
    )

    /**
     * Helper representing a game with a single black player.
     */
    class OnePlayer(dataAccess: GDAImpl, path: String) : Game(
        dataAccess = dataAccess,
        players = listOf(Player(PieceType.BLACK, 0, 32)),
        currGameName = path,
        board = Board(8),
        target = false,
        playerTurn = firstPlayerTurn,
    )

    /**
     * Helper representing a game with two players (black and white).
     */
    class TwoPlayers(dataAccess: GDAImpl, path: String) : Game(
        dataAccess = dataAccess,
        players = listOf(Player(PieceType.BLACK, 0, 32), Player(PieceType.WHITE, 0, 32)),
        currGameName = path,
        board = Board(8),
        target = false,
        playerTurn = firstPlayerTurn,
    )
}