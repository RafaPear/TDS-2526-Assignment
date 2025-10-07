package pt.isel.reversi.core.game

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.data.GDAImpl
import pt.isel.reversi.core.game.localgda.LocalGDA

/**
 * Lightweight test/dummy implementation of [GameImpl] used for data access and integration tests.
 *
 * Only acts as a structural carrier for required properties; behavioural methods are left as TODOs
 * so they surface if accidentally invoked in logic outside targeted tests. Replace with a richer
 * fake or real implementation when game rules are completed.
 */
@Suppress("unused")
open class MockGame(
    override val dataAccess: GDAImpl,
    override val players: List<Player>,
    override val currGameName: String?,
    override val board: Board,
    override val target: Boolean,
    override val isLocal: Boolean
) : GameImpl {
    override fun play(row: Int, col: Int): GameImpl? {
        TODO("Not yet implemented")
    }

    override fun start(piece: PieceType, name: String?): GameImpl? {
        TODO("Not yet implemented")
    }

    override fun pieceOptions(name: String): List<PieceType> {
        TODO("Not yet implemented")
    }

    override fun join(name: String, piece: PieceType): GameImpl? {
        TODO("Not yet implemented")
    }

    override fun setTargetMode(target: Boolean): GameImpl {
        TODO("Not yet implemented")
    }

    override fun getAvailablePlays(): List<Pair<Int, Int>> {
        TODO("Not yet implemented")
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

    fun copy(
        dataAccess: GDAImpl = this.dataAccess,
        players: List<Player> = this.players,
        currGameName: String? = this.currGameName,
        board: Board = this.board,
        target: Boolean = this.target,
        isLocal: Boolean = this.isLocal
    ) = MockGame(dataAccess, players, currGameName, board, target, isLocal)

    class EmptyPlayers(dataAccess: GDAImpl, path: String) : MockGame(
        dataAccess = dataAccess,
        players = emptyList(),
        currGameName = path,
        board = Board(8),
        target = false,
        isLocal = false
    )

    class OnePlayer(dataAccess: GDAImpl, path: String) : MockGame(
        dataAccess = dataAccess,
        players = listOf(Player(PieceType.BLACK, 0, 32)),
        currGameName = path,
        board = Board(8),
        target = false,
        isLocal = false
    )

    class TwoPlayers(dataAccess: GDAImpl, path: String) : MockGame(
        dataAccess = dataAccess,
        players = listOf(Player(PieceType.BLACK, 0, 32), Player(PieceType.WHITE, 0, 32)),
        currGameName = path,
        board = Board(8),
        target = false,
        isLocal = false
    )
}