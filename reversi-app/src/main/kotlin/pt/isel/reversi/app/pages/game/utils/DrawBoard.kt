package pt.isel.reversi.app.pages.game.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import pt.isel.reversi.app.BOARD_COLOR
import pt.isel.reversi.app.PRIMARY
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.storage.GameState

// Main Color Definitions
val TEXT_COLOR = Color.White           // Texto (pontuação)

val BOARD_BACKGROUND_COLOR = Color(0xFFB8860B)      // Fundo geral de madeira
val BOARD_SIDE_COLOR = Color(0xFFD2A679)

// Button configuration constants
val BUTTON_CONTENT_COLOR = Color.White
val BUTTON_MIN_FONT_SIZE = 12.sp
val BUTTON_MAX_FONT_SIZE = 40.sp
val BUTTON_TEXT_COLOR = TEXT_COLOR
val BUTTON_MAIN_COLOR = PRIMARY

const val GHOST_PIECE_ALPHA = 0.3f

@Composable
fun DrawBoard(
    target: Boolean,
    gameState: GameState,
    modifier: Modifier = Modifier,
    freeze: Boolean = false,
    getAvailablePlays: () -> List<Coordinate>,
    onCellClick: (coordinate: Coordinate) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(BOARD_SIDE_COLOR, shape = RoundedCornerShape(12.dp))
            .padding(all = 6.dp)
            .testTag(tag = testTagBoard())
    ) {
        Grid(target, gameState, modifier, freeze, getAvailablePlays) { coordinate -> onCellClick(coordinate) }
    }
}

/** Composable that draws the board grid */
@Composable
fun Grid(
    target: Boolean,
    gameState: GameState,
    modifier: Modifier = Modifier,
    freeze: Boolean = false,
    getAvailablePlays: () -> List<Coordinate>,
    onCellClick: (coordinate: Coordinate) -> Unit
) {
    val board: Board = gameState.board
    val side = board.side
    val playerTurn = gameState.lastPlayer.swap()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val availablePlays = if (target) getAvailablePlays() else emptyList<Coordinate>()

        val infiniteTransition = rememberInfiniteTransition()
        val alphaAnim by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                repeatMode = RepeatMode.Reverse
            )
        )
        val sizeAnim by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                repeatMode = RepeatMode.Reverse
            )
        )

        repeat(side) { y ->
            Row(
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                repeat(side) { x ->
                    val coordinate = Coordinate(x + 1, y + 1)
                    val ghostPiece = target && availablePlays.contains(coordinate)
                    val value = board[coordinate] ?: if (ghostPiece) playerTurn else null

                    val piece: Piece? =
                        if (value == null)
                            null
                        else
                            Piece(
                                coordinate = coordinate,
                                value = value,
                                isGhostPiece = ghostPiece
                            )

                    cellView(
                        coordinate,
                        piece = piece,
                        freeze,
                        modifier = modifier.weight(1f),
                        radiusModifier = if (ghostPiece) sizeAnim else 1f,
                        alphaModifier = if (ghostPiece) alphaAnim else 1f,
                    ) {
                        onCellClick(coordinate)
                    }
                }
            }
        }
    }
}

@Composable
fun cellView(
    coordinate: Coordinate,
    piece: Piece?,
    freeze: Boolean = false,
    modifier: Modifier = Modifier,
    radiusModifier: Float = 1f,
    alphaModifier: Float = 1f,
    onClick: (coordinate: Coordinate) -> Unit
) {
    val clickable = !freeze && (piece == null || piece.isGhostPiece)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BOARD_COLOR)
            .clickable(enabled = clickable) { onClick(coordinate) }
            .testTag(testTagCellView(coordinate)),
        contentAlignment = Alignment.Center
    ) {
        val type = piece?.value ?: return@Box

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .semantics { testTag = testTagPiece(coordinate, type = type) }
        ) {
            val size = size
            val radius = size.minDimension / 2 * 0.7f
            val center = Offset(size.width / 2, size.height / 2)

            val color = when (type) {
                PieceType.BLACK -> Color.Black
                PieceType.WHITE -> Color.White
            }

            if (!piece.isGhostPiece) {
                drawPiece(radius, center, color, drawScope = this)
            } else {
                drawCircle(
                    color = color.copy(alpha = GHOST_PIECE_ALPHA),
                    radius = radius * radiusModifier,
                    center = center,
                    alpha = alphaModifier,
                )
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


