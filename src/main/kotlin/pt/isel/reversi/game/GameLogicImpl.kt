package pt.isel.reversi.game

import pt.isel.reversi.board.PieceType
import pt.isel.reversi.board.Board

interface GameLogicImpl {

    fun play(board: Board, row: Int, col: Int): Board?

    fun getAvailablePlays(board: Board, piece: PieceType): List<Pair<Int, Int>>

    // TODO FAZER RESTOOOOOOOOOOOOOO
}