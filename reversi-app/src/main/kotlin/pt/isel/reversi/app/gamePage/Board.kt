package pt.isel.reversi.app.gamePage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType

val padding = 20.dp

// Main Color Definitions
val TEXT_COLOR = Color.Black           // Texto (pontuação)

val BOARD_BACKGROUND_COLOR = Color(0xFFB8860B)      // Fundo geral de madeira
val BOARD_SIDE_COLOR = Color(0xFFD2A679)     // Painel superior
val BOARD_MAIN_COLOR = Color(0xFF4CAF50)          // Verde principal do tabuleiro

// Button configuration constants
val BUTTON_CONTENT_COLOR = Color.White
val BUTTON_MIN_FONT_SIZE = 12.sp
val BUTTON_MAX_FONT_SIZE = 40.sp
val BUTTON_TEXT_COLOR = TEXT_COLOR
val BUTTON_MAIN_COLOR = Color(0xFF4CAF50)

@Composable
fun DrawBoard(game: Game, modifier: Modifier = Modifier, onCellClick: (x: Int, y: Int) -> Unit) {

    val state = game.gameState

    if (state != null)
        Grid(game, modifier) { x, y -> onCellClick(x, y) }
}

/** Composable that draws the board grid */
@Composable
fun Grid(
    game: Game,
    modifier: Modifier = Modifier,
    onCellClick: (x: Int, y: Int) -> Unit
) {
    val board: Board = game.gameState?.board ?: return
    val side = board.side
    val target = game.target
    val playerTurn = game.gameState?.lastPlayer?.swap()


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(side) { y ->
            Row(
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                val availablePlays = game.getAvailablePlays()

                repeat(side) { x ->
                    val coordinate = Coordinate(x + 1, y + 1)
                    val cellValue = board[coordinate]
                    val isTargetCell = target && availablePlays.contains(coordinate)
                    Box(
                        modifier = modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BOARD_MAIN_COLOR)
                            .clickable(enabled = cellValue == null) { onCellClick(x + 1, y + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = modifier
                                .fillMaxSize()
                                .aspectRatio(1f)
                        ) {
                            val size = size
                            val radius = size.minDimension / 2 * 0.7f
                            val center = Offset(size.width / 2, size.height / 2)

                            val color = when (cellValue) {
                                PieceType.BLACK -> Color.Black
                                PieceType.WHITE -> Color.White
                                else -> null
                            }
                            if (color != null) {
                                drawPiece(radius, center, color, drawScope = this)
                            } else if (isTargetCell && playerTurn != null) {
                                drawCircle(
                                    color = (if (playerTurn == PieceType.BLACK) Color.Black else Color.White).copy(alpha = 0.3f),
                                    radius = radius,
                                    center = center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


fun drawPiece(
    radius: Float,
    center: Offset,
    color: Color,
    drawScope: DrawScope
) {
    // Sombra grande, oval, deslocada diagonal para baixo-direita
    val shadowColor = Color(0xFF000000).copy(alpha = 0.3f)
    drawScope.drawCircle(
        color = shadowColor,
        radius = radius,
        center = center + Offset(15f, 15f),
    )

    // lateral
    val sideColor = if (color == Color.White) Color(0xFFB0B4C1) else Color(0xFF222222)
    drawScope.drawCircle(
        color = sideColor,
        radius = radius,
        center = center + Offset(5f, 5f),
    )

    // Parte superior da peça
    drawScope.drawCircle(
        color = color,
        radius = radius,
        center = center - Offset(3f, 3f),
    )
}


