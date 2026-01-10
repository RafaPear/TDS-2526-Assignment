package pt.isel.reversi.app.pages.mainmenu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.getTheme
import java.util.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val MAX_PIECES = 50
private const val SPAWN_DELAY_MS = 250L
private const val WAVE_DURATION_MS = 3000
private const val SPECIAL_PIECE_CHANCE = .1f
private const val SPECIAL_PIECES_MAX = 2f
private const val BASE_ALPHA = 0.3f
private const val FADE_EDGE_WIDTH = 0.7f

/**
 * Represents an individual animated piece in the background.
 */
data class MovingPiece(
    val id: Long,
    val xPercent: Float,
    val yPercent: Float,
    val radiusDp: Float,
    val speed: Float,
    val phase: Float,
    val waveAmplitude: Float,
    val isWhite: Boolean,
    val spawnTime: Long,
    val isSpecial: Boolean = false
)

@Composable
fun ReversiScope.AnimatedBackground() {
    val theme = getTheme()
    val activePieces = remember { mutableStateListOf<MovingPiece>() }
    val infiniteTransition = rememberInfiniteTransition(label = "oscillation")

    // Smooth vertical wave oscillation
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(WAVE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Spawn new pieces periodically
    LaunchedEffect(Unit) {
        while (isActive) {
            if (activePieces.size < MAX_PIECES) {
                val canAddSpecial = activePieces.count { it.isSpecial } <= SPECIAL_PIECES_MAX && Random.nextFloat() < SPECIAL_PIECE_CHANCE
                activePieces.add(createNewPiece(canAddSpecial))
            }
            activePieces.removeAll { it.xPercent > 1.2f }
            delay(SPAWN_DELAY_MS)
        }
    }

    // Animation loop to update horizontal positions every frame
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos {
                for (i in activePieces.indices) {
                    val piece = activePieces[i]
                    activePieces[i] = piece.copy(xPercent = piece.xPercent + piece.speed)
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        activePieces.forEach { piece ->
            val x = piece.xPercent * size.width
            val y = (piece.yPercent * size.height) + calculateWaveOffset(waveOffset, piece.phase, piece.waveAmplitude)
            val edgeFade = calculateEdgeFade(piece.xPercent)
            val radiusPx = piece.radiusDp.dp.toPx()

            drawPiece(piece, theme, x, y, radiusPx, edgeFade)

            if (piece.isSpecial) {
                drawSpecialFeatures(x, y, radiusPx, edgeFade)
            }
        }
    }
}
private fun createNewPiece(canAddSpecial: Boolean): MovingPiece {
    val size = Random.nextInt(20, 60).toFloat()

    val speedBase = 0.08f / size
    val speedVariation = Random.nextFloat() * 0.001f
    val finalSpeed = speedBase + speedVariation

    val waveAmplitude = (65f - size).coerceIn(10f, 35f)

    return MovingPiece(
        id = UUID.randomUUID().mostSignificantBits, // ID mais seguro
        xPercent = -FADE_EDGE_WIDTH,
        yPercent = Random.nextFloat(),
        radiusDp = size,
        speed = finalSpeed,
        phase = Random.nextFloat() * 2f * PI.toFloat(),
        waveAmplitude = waveAmplitude,
        isWhite = Random.nextBoolean(),
        spawnTime = System.currentTimeMillis(),
        isSpecial = canAddSpecial && Random.nextFloat() < SPECIAL_PIECE_CHANCE
    )
}

private fun calculateWaveOffset(waveOffset: Float, phase: Float, amplitude: Float): Float {
    return sin(waveOffset + phase) * amplitude
}

private fun calculateEdgeFade(xPercent: Float): Float {
    return when {
        xPercent < 0f -> (xPercent + FADE_EDGE_WIDTH) / FADE_EDGE_WIDTH
        xPercent > 1f -> 1f - ((xPercent - 1f) / FADE_EDGE_WIDTH)
        else -> 1f
    }.coerceIn(0f, 1f)
}

private fun DrawScope.drawPiece(
    piece: MovingPiece,
    theme: AppTheme,
    x: Float,
    y: Float,
    radiusPx: Float,
    edgeFade: Float
) {
    val baseColor = if (piece.isWhite) theme.lightPieceColor else theme.darkPieceColor
    val alpha = BASE_ALPHA * edgeFade

    // Main piece circle
    drawCircle(
        color = baseColor.copy(alpha = alpha),
        radius = radiusPx,
        center = Offset(x, y)
    )

    // Outline
    drawCircle(
        color = theme.lightPieceColor.copy(alpha = alpha * 1.2f),
        radius = radiusPx,
        center = Offset(x, y),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawSpecialFeatures(
    x: Float,
    y: Float,
    radiusPx: Float,
    edgeFade: Float
) {
    val detailAlpha = (BASE_ALPHA * edgeFade * 3f).coerceAtMost(1f)

    // Eyes
    drawEyes(x, y, radiusPx, detailAlpha)

    // Crown
    drawCrown(x, y, radiusPx, detailAlpha)
}

private fun DrawScope.drawEyes(
    x: Float,
    y: Float,
    radiusPx: Float,
    alpha: Float
) {
    val eyeOffset = radiusPx * 0.3f
    val eyeSize = radiusPx * 0.12f
    val eyeColor = Color.White
    val eyeY = y - eyeOffset * 0.1f

    // Left eye
    drawCircle(
        color = eyeColor.copy(alpha = alpha),
        radius = eyeSize,
        center = Offset(x - eyeOffset, eyeY)
    )

    // Right eye
    drawCircle(
        color = eyeColor.copy(alpha = alpha),
        radius = eyeSize,
        center = Offset(x + eyeOffset, eyeY)
    )
}

private fun DrawScope.drawCrown(
    x: Float,
    y: Float,
    radiusPx: Float,
    alpha: Float
) {
    val crownWidth = radiusPx * 0.9f
    val crownHeight = radiusPx * 0.5f
    val crownTop = y - radiusPx - (3.dp.toPx() * 2)
    val crownBase = y - radiusPx * 0.4f

    val crownPath = Path().apply {
        moveTo(x - crownWidth / 2, crownBase)
        lineTo(x - crownWidth / 2, crownTop - crownHeight * 0.5f)
        lineTo(x - crownWidth / 4, crownTop + crownHeight * 0.2f)
        lineTo(x, crownTop - crownHeight)
        lineTo(x + crownWidth / 4, crownTop + crownHeight * 0.2f)
        lineTo(x + crownWidth / 2, crownTop - crownHeight * 0.5f)
        lineTo(x + crownWidth / 2, crownBase)
        close()
    }

    drawPath(
        path = crownPath,
        color = Color(0xFFFFD700).copy(alpha = alpha)
    )
}