package pt.isel.reversi.game

import pt.isel.reversi.board.PieceType

data class Player(
    val type: PieceType,
    val points: Int,
    val playsLeft: Int
)