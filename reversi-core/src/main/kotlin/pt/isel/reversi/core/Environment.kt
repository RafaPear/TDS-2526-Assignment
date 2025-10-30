package pt.isel.reversi.core

import pt.isel.reversi.core.board.PieceType
import  pt.isel.reversi.core.game.localgda.LocalGDA

object Environment {
    const val BOARD_SIDE = 8
    val First_Player_TURN = PieceType.BLACK

    val DATA_ACCESS = LocalGDA()
}