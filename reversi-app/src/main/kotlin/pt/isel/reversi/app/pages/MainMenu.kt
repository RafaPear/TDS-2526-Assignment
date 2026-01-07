package pt.isel.reversi.app.pages

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.isel.reversi.app.*
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setPage
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

val MAIN_MENU_AUTO_SIZE_BUTTON_TEXT =
    TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 24.sp)
/**
 * Representa uma peça individual na animação de fundo.
 */
data class MovingPiece(
    val id: Long,
    val xPercent: Float,
    val yPercent: Float,
    val radiusDp: Float,
    val speed: Float,
    val phase: Float,
    val isWhite: Boolean,
    val spawnTime: Long,
    val isSpecial: Boolean = false
)

@Composable
fun ReversiScope.AnimatedBackground() {
    val theme = getTheme()
    val activePieces = remember { mutableStateListOf<MovingPiece>() }
    val infiniteTransition = rememberInfiniteTransition(label = "oscillation")

    // Oscilação vertical suave para as peças
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    LaunchedEffect(Unit) {
        while (true) {
            if (activePieces.size < 50) {
                val canAddSpecial = activePieces.none { it.isSpecial }
                activePieces.add(
                    MovingPiece(
                        id = System.nanoTime(),
                        xPercent = -0.2f,
                        yPercent = Random.nextFloat(),
                        radiusDp = Random.nextInt(24, 52).toFloat(),
                        speed = Random.nextFloat().coerceIn(0.0005f, 0.0009f),
                        phase = Random.nextFloat() * 2f * PI.toFloat(),
                        isWhite = Random.nextBoolean(),
                        spawnTime = System.currentTimeMillis(),
                        isSpecial = canAddSpecial && Random.nextFloat() < 0.1f
                    )
                )
            }

            activePieces.removeAll { it.xPercent > 1.2f }
            delay(500)
        }
    }

    // Loop de animação para atualizar a posição horizontal a cada frame
    val frameClock = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameClock.value = it }
            for (i in activePieces.indices) {
                val p = activePieces[i]
                activePieces[i] = p.copy(xPercent = p.xPercent + p.speed)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        activePieces.forEach { p ->
            val x = p.xPercent * size.width
            val y = (p.yPercent * size.height) + (sin(waveOffset + p.phase) * 20f)

            // Efeito de fade-in e fade-out nas bordas do ecrã
            val edgeFade = when {
                p.xPercent < 0f -> (p.xPercent + 0.2f) / 0.2f
                p.xPercent > 1f -> 1f - ((p.xPercent - 1f) / 0.2f)
                else -> 1f
            }.coerceIn(0f, 1f)

            val baseColor = if (p.isWhite) theme.lightPieceColor else theme.darkPieceColor
            val alpha = 0.15f * edgeFade
            val radiusPx = p.radiusDp.dp.toPx()
            val thickness = 3.dp.toPx()

            // Face da peça
            drawCircle(
                color = baseColor.copy(alpha = alpha),
                radius = radiusPx,
                center = Offset(x, y)
            )

            // Contorno fino
            drawCircle(
                color = theme.lightPieceColor.copy(alpha = alpha * 1.2f),
                radius = radiusPx,
                center = Offset(x, y),
                style = Stroke(width = 1.dp.toPx())
            )

            // --- 2. Elementos Especiais (Coroa e Olhos) ---

            if (p.isSpecial) {
                val detailAlpha = (alpha * 3f).coerceAtMost(1f)

                // Olhinhos (Dois pequenos círculos)
                val eyeOffset = radiusPx * 0.3f
                val eyeSize = radiusPx * 0.12f
                val eyeColor = Color.White
                drawCircle(
                    color = eyeColor.copy(alpha = detailAlpha),
                    radius = eyeSize,
                    center = Offset(x - eyeOffset, y - eyeOffset * 0.1f)
                )
                drawCircle(
                    color = eyeColor.copy(alpha = detailAlpha),
                    radius = eyeSize,
                    center = Offset(x + eyeOffset, y - eyeOffset * 0.1f)
                )

                // Coroa (Um polígono simples de 3 pontas)
                val crownWidth = radiusPx * 0.9f
                val crownHeight = radiusPx * 0.5f
                val crownTop = y - radiusPx - (thickness * 2)
                val crownBase = y - radiusPx * 0.4f

                val crownPath = Path().apply {
                    moveTo(x - crownWidth / 2, crownBase)
                    lineTo(x - crownWidth / 2, crownTop - crownHeight * 0.5f) // Ponta esquerda
                    lineTo(x - crownWidth / 4, crownTop + crownHeight * 0.2f) // Vale 1
                    lineTo(x, crownTop - crownHeight)                        // Ponta central
                    lineTo(x + crownWidth / 4, crownTop + crownHeight * 0.2f) // Vale 2
                    lineTo(x + crownWidth / 2, crownTop - crownHeight * 0.5f) // Ponta direita
                    lineTo(x + crownWidth / 2, crownBase)
                    close()
                }

                drawPath(
                    path = crownPath,
                    color = Color(0xFFFFD700).copy(alpha = detailAlpha) // Cor de Ouro
                )
            }
        }
    }
}

@Composable
fun MainMenu(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(appState.value.page) {
        val audioPool = appState.getStateAudioPool()
        val theme = appState.value.theme
        if (!audioPool.isPlaying(theme.backgroundMusic)) {
            audioPool.stopAll()
            audioPool.play(theme.backgroundMusic)
        }
    }

    ScaffoldView(appState = appState, previousPageContent = {}) {
        Box(modifier = Modifier.fillMaxSize()) {

            AnimatedBackground()

            Column(
                modifier = modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReversiText(text = "REVERSI", fontWeight = FontWeight.Black, fontSize = 80.sp)
                Spacer(Modifier.height(40.dp))

                Column(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReversiButton("Novo Jogo") { appState.setPage(Page.NEW_GAME) }
                    ReversiButton("Lobby") { appState.setPage(Page.LOBBY) }
                    ReversiButton("Definições") { appState.setPage(Page.SETTINGS) }
                    ReversiButton("Sobre") { appState.setPage(Page.ABOUT) }
                }
            }
        }
    }
}