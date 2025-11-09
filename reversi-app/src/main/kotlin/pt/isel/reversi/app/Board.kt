package pt.isel.reversi.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import pt.isel.reversi.core.BOARD_SIDE


val WINDOW_MIN_SIZE = java.awt.Dimension(600, 500)

val padding = 20.dp
// Board configuration constants
object BoardConfig {
    const val HEIGHT_FRACTION = 0.8f
    const val WIDTH_FRACTION = 0.6f
    val COLOR = Color(0xFF800080)

    val LINE_COLOR = Color.Black
    const val LINE_STROKE_WIDTH = 4f
}

object ButtonConfig {
    const val HEIGHT_FRACTION = 0.6f
    const val WIDTH_FRACTION = 0.2f
    val CONTAINER_COLOR = Color.Blue
    val CONTENT_COLOR = Color.White

    //Text
    val MIN_FONT_SIZE = 12.sp
    val MAX_FONT_SIZE = 40.sp
}
fun main() = application {
    val windowState = rememberWindowState(position = WindowPosition.PlatformDefault)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
    ) {
        this.window.minimumSize = WINDOW_MIN_SIZE
        Board()
    }
}

/** Main composable displaying the board and buttons */
@Preview
@Composable
fun Board() {
    var target by remember { mutableStateOf("On") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
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
            .fillMaxHeight(ButtonConfig.HEIGHT_FRACTION)
            .fillMaxWidth(ButtonConfig.WIDTH_FRACTION),
        colors = buttonColors(
            containerColor = ButtonConfig.CONTAINER_COLOR,
            contentColor = ButtonConfig.CONTENT_COLOR
        ),
        onClick = onClick
    ) {
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased(
                minFontSize = ButtonConfig.MIN_FONT_SIZE,
                maxFontSize = ButtonConfig.MAX_FONT_SIZE
            )
        )
    }
}

/** Composable that draws the board grid */
@Composable
fun Grid() {
    Box(
        modifier = Modifier
            .background(BoardConfig.COLOR)
            .fillMaxHeight(BoardConfig.HEIGHT_FRACTION)
            .fillMaxWidth(BoardConfig.WIDTH_FRACTION),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGrid(BOARD_SIDE, this) // Draw the grid lines
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
            color = BoardConfig.LINE_COLOR,
            strokeWidth = BoardConfig.LINE_STROKE_WIDTH,
            start = Offset(x * cellWidth, 0f),
            end = Offset(x * cellWidth, size.height)
        )
    }

    // Horizontal lines
    for (y in 1 until side) {
        drawLine(
            color = BoardConfig.LINE_COLOR,
            strokeWidth = BoardConfig.LINE_STROKE_WIDTH,
            start = Offset(0f, y * cellHeight),
            end = Offset(size.width, y * cellHeight)
        )
    }
}
