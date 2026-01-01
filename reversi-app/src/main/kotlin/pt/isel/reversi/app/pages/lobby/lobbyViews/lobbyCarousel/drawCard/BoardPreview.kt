package pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.pages.game.utils.BOARD_BACKGROUND_COLOR
import pt.isel.reversi.app.pages.lobby.BOARD_COLOR
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType

@Composable
fun BoardPreview(board: Board, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp))
            .background(BOARD_BACKGROUND_COLOR)
            .padding(6.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            repeat(board.side) { y ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    repeat(board.side) { x ->
                        val piece = board[Coordinate(x + 1, y + 1)]
                        Cell(piece, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(piece: PieceType?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape = RoundedCornerShape(6.dp))
            .aspectRatio(1f)
            .background(BOARD_COLOR),
    ) {
        if (piece != null) {
            Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                val radius = size.minDimension / 2 * 0.7f
                val center = Offset(size.width / 2, size.height / 2)
                val color = if (piece == PieceType.BLACK) Color.Black else Color.White


                val sideColor = if (color == Color.White) Color(0xFFCFD8DC) else Color(0xFF37474F)
                drawCircle(sideColor, radius, center + Offset(1f, 1f))

                drawCircle(color.copy(alpha = .4f), radius * 0.95f, center)

                drawCircle(
                    Color.White.copy(if (color == Color.White) 0.6f else 0.35f),
                    radius * 0.25f,
                    center - Offset(radius * 0.35f, radius * 0.35f)
                )
            }
        }
    }
}