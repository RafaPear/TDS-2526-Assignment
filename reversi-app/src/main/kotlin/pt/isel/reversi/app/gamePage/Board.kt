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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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

const val GHOST_PIECE_ALPHA = 0.3f

fun getBoardTestTag() = "ReversiBoard"

@Composable
fun DrawBoard(
    game: Game,
    modifier: Modifier = Modifier,
    freeze: Boolean = false,
    onCellClick: (coordinate: Coordinate) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(BOARD_SIDE_COLOR, shape = RoundedCornerShape(12.dp))
            .padding(all = 10.dp)
            .testTag(tag = getBoardTestTag())
    ) {
        val state = game.gameState

        if (state != null)
            Grid(game, modifier, freeze) { coordinate -> onCellClick(coordinate) }
    }
}

/** Composable that draws the board grid */
@Composable
fun Grid(
    game: Game,
    modifier: Modifier = Modifier,
    freeze: Boolean = false,
    onCellClick: (coordinate: Coordinate) -> Unit
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
        val availablePlays = game.getAvailablePlays()

        repeat(side) { y ->
            Row(
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                repeat(side) { x ->
                    val coordinate = Coordinate(x + 1, y + 1)
                    val cellValue = board[coordinate]
                    val ghostPiece = if (target && availablePlays.contains(coordinate)) playerTurn else null

                    cellView(coordinate, cellValue, ghostPiece, freeze, modifier = modifier.weight(1f)) {
                        onCellClick(coordinate)
                    }
                }
            }
        }
    }
}

fun getCellViewTestTag(coordinate: Coordinate) =
    "cell_${coordinate.row},${coordinate.col}"

fun getPieceTestTag(coordinate: Coordinate, type: PieceType?): String {
    val value = when (type) {
        PieceType.BLACK -> "BLACK"
        PieceType.WHITE -> "WHITE"
        null -> ""
    }
    return "Piece_${getCellViewTestTag(coordinate)}_${value}"
}

@Composable
fun cellView(
    coordinate: Coordinate,
    cellValue: PieceType?,
    ghostPiece: PieceType?,
    freeze: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (coordinate: Coordinate) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BOARD_MAIN_COLOR)
            .clickable(enabled = (cellValue == null && !freeze)) { onClick(coordinate) }
            .testTag(getCellViewTestTag(coordinate)),
        contentAlignment = Alignment.Center
    ) {
        if (cellValue != null || ghostPiece != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .semantics { testTag = getPieceTestTag(coordinate, cellValue ?: ghostPiece) }
            ) {
                val size = size
                val radius = size.minDimension / 2 * 0.7f
                val center = Offset(size.width / 2, size.height / 2)

                if (cellValue != null) {
                    val color = when (cellValue) {
                        PieceType.BLACK -> Color.Black
                        PieceType.WHITE -> Color.White
                    }
                    drawPiece(radius, center, color, drawScope = this)
                } else if (ghostPiece != null) {
                    val color = when (ghostPiece) {
                        PieceType.BLACK -> Color.Black
                        PieceType.WHITE -> Color.White
                    }
                    drawCircle(
                        color = color.copy(alpha = GHOST_PIECE_ALPHA),
                        radius = radius,
                        center = center,
                    )
                }
            }
        }
    }
}


private fun drawPiece(
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


