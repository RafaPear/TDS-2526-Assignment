package pt.isel.reversi.game

import pt.isel.reversi.board.PieceType
import pt.isel.reversi.board.Board

interface GameImpl {
    val dataAccess: GameDataAccessImpl
    val players: List<Player>
    val currGameName: String?
    val board: Board

    val target: Boolean
    val isLocal: Boolean
        // TODO get() = players.size > 1

    // TODO Must have init{}

    fun play(row: Int, col: Int): GameImpl?

    fun start(piece: PieceType, name: String? = null): GameImpl?

    fun pieceOptions(name: String): List<PieceType>

    fun join(name: String, piece: PieceType): GameImpl?

    fun setTargetMode(target: Boolean): GameImpl

    fun getAvailablePlays(): List<Pair<Int, Int>>

    fun pass()

    fun refresh(): GameImpl

    fun poopBoard(): Board
}