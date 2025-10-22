package pt.isel.reversi.core.game

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.exceptions.InvalidPlayException

// TODO: Organize const values
val firstPlayerTurn = PieceType.BLACK
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
    override fun play(coordinate: Coordinate): GameImpl {
        var newBoard = board ?: throw InvalidPlayException(
            message = "Game is not started yet."
        )

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

        if (currGameName != null) {
            dataAccess.postPiece(currGameName!!, piece)
        }

        return this.copy(
            board = newBoard,
            players = newPlayers,
            playerTurn = nextPlayerTurn
        )
    }

    override fun pieceOptions(): List<PieceType> {
        if (currGameName == null) return emptyList()
        return dataAccess.getAvailablePieces(currGameName!!)
    }

    override fun setTargetMode(target: Boolean): GameImpl =
        this.copy(target = target)

    override fun getAvailablePlays(): List<Coordinate> {
        if (players.size == 1 && players[0].type != playerTurn) {
            return emptyList()
        }

        return GameLogic().getAvailablePlays(
            board = board ?: throw InvalidPlayException(
                message = "Game is not started yet."
            ),
            myPieceType = playerTurn
        )
    }

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

    override fun pass() {
        TODO("Not yet implemented")
    }

    override fun refresh(): GameImpl {
        TODO("Not yet implemented")
    }

    override fun poopBoard(): Board {
        TODO("Not yet implemented")
    }

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