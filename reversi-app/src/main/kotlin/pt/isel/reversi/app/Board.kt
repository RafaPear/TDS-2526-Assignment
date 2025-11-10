package pt.isel.reversi.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.core.BOARD_SIDE

val WINDOW_MIN_SIZE = java.awt.Dimension(600, 500)

val padding = 20.dp

// Main Color Definitions
val TEXT_COLOR = Color.Black           // Texto (pontuação)

// Board configuration constants
const val BOARD_HEIGHT_FRACTION = 0.8f
const val BOARD_WIDTH_FRACTION = 0.6f
const val BOARD_LINE_STROKE_WIDTH = 4f
val BOARD_BACKGROUND_COLOR = Color(0xFFB8860B)      // Fundo geral de madeira
val BOARD_SIDE_COLOR = Color(0xFFD2A679)     // Painel superior
val BOARD_MAIN_COLOR = Color(0xFF4CAF50)          // Verde principal do tabuleiro
val BOARD_LINE_COLOR = Color(0xFF3E8E41)           // Linhas da grade

// Button configuration constants
const val BUTTON_HEIGHT_FRACTION = 0.6f
const val BUTTON_WIDTH_FRACTION = 0.2f
val BUTTON_CONTENT_COLOR = Color.White
val BUTTON_MIN_FONT_SIZE = 12.sp
val BUTTON_MAX_FONT_SIZE = 40.sp
val BUTTON_TEXT_COLOR = TEXT_COLOR
val BUTTON_MAIN_COLOR = Color(0xFF4CAF50)

/** Main composable displaying the board and buttons */
@Preview
@Composable
fun Board() {
    var target by remember { mutableStateOf("On") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BOARD_BACKGROUND_COLOR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Grid()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = padding),
            horizontalArrangement = Arrangement.Center
        ) {
            // Button to toggle the target state
            GameButton("Target $target") { target = if (target == "On") "Off" else "On" }

            Spacer(modifier = Modifier.width(padding))

            // Main action button
            GameButton("PLAY") { /* button action */ }
        }
    }
}

/** Composable button with auto-sizing text */
@Composable
fun GameButton(label: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxHeight(BUTTON_HEIGHT_FRACTION)
            .fillMaxWidth(BUTTON_WIDTH_FRACTION),
        colors = buttonColors(
            containerColor = BUTTON_MAIN_COLOR,
            contentColor = BUTTON_CONTENT_COLOR
        ),
        onClick = onClick
    ) {
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased(
                minFontSize = BUTTON_MIN_FONT_SIZE,
                maxFontSize = BUTTON_MAX_FONT_SIZE
            ),
            color = BUTTON_TEXT_COLOR
        )
    }
}

/** Composable that draws the board grid */
@Composable
fun Grid() {
    Box(
        modifier = Modifier
            .fillMaxHeight(BOARD_HEIGHT_FRACTION)
            .fillMaxWidth(BOARD_WIDTH_FRACTION)
            .background(BOARD_SIDE_COLOR, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.96f)) {
            drawRoundRect(
                color = BOARD_MAIN_COLOR,
            )
            drawGrid(BOARD_SIDE, this)
        }
    }
}

/**
 * Draws the grid on the given DrawScope
 * @param side number of cells per board side
 * @param drawScope the DrawScope where the grid will be drawn
 */
fun drawGrid(side: Int, drawScope: DrawScope) = with(drawScope) {
    val cellWidth = size.width / side
    val cellHeight = size.height / side

    // Vertical lines
    for (x in 1 until side) {
        drawLine(
            color = BOARD_LINE_COLOR,
            strokeWidth = BOARD_LINE_STROKE_WIDTH,
            start = Offset(x * cellWidth, 0f),
            end = Offset(x * cellWidth, size.height)
        )
    }

    // Horizontal lines
    for (y in 1 until side) {
        drawLine(
            color = BOARD_LINE_COLOR,
            strokeWidth = BOARD_LINE_STROKE_WIDTH,
            start = Offset(0f, y * cellHeight),
            end = Offset(size.width, y * cellHeight)
        )
    }
}

