package pt.isel.reversi.app.pages.game

import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType

fun testTagTitle(gameName: String?) =
    "game_page_title_${gameName ?: "null"}"

fun testTagBoard() = "game_page_board"
fun testTagCellView(coordinate: Coordinate) =
    "cell_${coordinate.row},${coordinate.col}"

fun testTagPiece(coordinate: Coordinate, type: PieceType?): String {
    val value = when (type) {
        PieceType.BLACK -> "BLACK"
        PieceType.WHITE -> "WHITE"
        null            -> ""
    }
    return "Piece_${testTagCellView(coordinate)}_${value}"
}

fun testTagGamePage() = "game_page"

fun testTagPlayerScore(player: Player) =
    "player_score_${if (player.type == PieceType.BLACK) "BLACK" else "WHITE"}_${player.points}"

fun testTagTargetButtons(target: Boolean) = "game_page_target_buttons_${if (target) "ON" else "OFF"}"