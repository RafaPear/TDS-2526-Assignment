package pt.isel.reversi.app.pages.game

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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.getTheme
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType

const val GHOST_PIECE_ALPHA = 0.5f // Aumentei ligeiramente para melhor visibilidade em cores exóticas

@Composable
fun ReversiScope.DrawBoard(
    game: Game,
    modifier: Modifier = Modifier,
    freeze: Boolean = false,
    onCellClick: (coordinate: Coordinate) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(getTheme().boardSideColor, shape = RoundedCornerShape(12.dp))
            .padding(all = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag(tag = testTagBoard())
    ) {

        val state = game.gameState

        if (state != null)
            Grid(game, modifier, freeze) { coordinate -> onCellClick(coordinate) }
    }
}

/** Composable that draws the board grid */
@Composable
fun ReversiScope.Grid(
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
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(getTheme().boardBgColor)
            .padding(2.dp)
            .testTag(testTagBoard()),
    ) {
        val availablePlays = game.getAvailablePlays()

        val infiniteTransition = rememberInfiniteTransition()
        val alphaAnim by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0.4f, // Ajustado para ser subtil independentemente da cor
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                repeatMode = RepeatMode.Reverse
            )
        )
        val sizeAnim by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.85f,
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
fun ReversiScope.cellView(
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
            .clip(RoundedCornerShape(12.dp))
            .background(getTheme().boardColor)
            .clickable(enabled = clickable) { onClick(coordinate) }
            .testTag(testTagCellView(coordinate)),
        contentAlignment = Alignment.Center
    ) {
        val type = piece?.value ?: return@Box

        // AQUI: Obtemos as cores dinâmicas do tema atual
        val theme = getTheme()
        val pieceColor = when (type) {
            PieceType.BLACK -> theme.darkPieceColor
            PieceType.WHITE -> theme.lightPieceColor
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .semantics { testTag = testTagPiece(coordinate, type = type) }
        ) {
            val size = size
            val radius = size.minDimension / 2 * 0.7f
            val center = Offset(size.width / 2, size.height / 2)

            if (!piece.isGhostPiece) {
                drawPiece(radius, center, pieceColor, drawScope = this)
            } else {
                // Ghost Piece simplificada usando a cor do tema com transparência
                drawCircle(
                    color = pieceColor.copy(alpha = GHOST_PIECE_ALPHA),
                    radius = radius * radiusModifier,
                    center = center,
                    alpha = alphaModifier,
                )
                // Opcional: Desenhar um pequeno outline para ghost pieces claras em fundos claros
                drawCircle(
                    color = pieceColor.copy(alpha = alphaModifier * 0.5f),
                    radius = radius * radiusModifier,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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
    // Sombra da peça (mantemos preto transparente pois é sombra)
    val shadowColor = Color.Black.copy(alpha = 0.3f)
    drawScope.drawCircle(
        color = shadowColor,
        radius = radius,
        center = center + Offset(10f, 10f), // Sombra ligeiramente mais subtil
    )

    // AQUI: Cálculo dinâmico da cor lateral (efeito 3D)
    // Escurece a cor original em 30% para criar a borda, independentemente da cor escolhida
    val sideColor = Color(
        red = (color.red * 0.7f).coerceIn(0f, 1f),
        green = (color.green * 0.7f).coerceIn(0f, 1f),
        blue = (color.blue * 0.7f).coerceIn(0f, 1f),
        alpha = color.alpha
    )

    drawScope.drawCircle(
        color = sideColor,
        radius = radius,
        center = center + Offset(4f, 4f),
    )

    // Parte superior da peça (Cor do tema)
    drawScope.drawCircle(
        color = color,
        radius = radius,
        center = center - Offset(2f, 2f),
    )
}