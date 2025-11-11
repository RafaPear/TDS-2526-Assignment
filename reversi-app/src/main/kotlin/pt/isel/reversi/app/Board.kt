package pt.isel.reversi.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType

val WINDOW_MIN_SIZE = java.awt.Dimension(600, 500)

val padding = 20.dp

// Main Color Definitions
val TEXT_COLOR = Color.Black           // Texto (pontuação)

// Board configuration constants
const val BOARD_LINE_STROKE_WIDTH = 4f
val BOARD_BACKGROUND_COLOR = Color(0xFFB8860B)      // Fundo geral de madeira
val BOARD_SIDE_COLOR = Color(0xFFD2A679)     // Painel superior
val BOARD_MAIN_COLOR = Color(0xFF4CAF50)          // Verde principal do tabuleiro
val BOARD_LINE_COLOR = Color(0xFF3E8E41)           // Linhas da grade

// Button configuration constants
val BUTTON_CONTENT_COLOR = Color.White
val BUTTON_MIN_FONT_SIZE = 12.sp
val BUTTON_MAX_FONT_SIZE = 40.sp
val BUTTON_TEXT_COLOR = TEXT_COLOR
val BUTTON_MAIN_COLOR = Color(0xFF4CAF50)

/** Main composable displaying the board and buttons */
@Preview
@Composable
fun Board(game: MutableState<Game>, onCellClick: (x: Int, y: Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BOARD_BACKGROUND_COLOR)
            .padding(20.dp)
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var target by remember { mutableStateOf(
            if (game.value.target) "On" else "Off"
        ) }
        Row(
            modifier = Modifier
                .aspectRatio(1f)
                .weight(5f),
        ) {
            val state = game.value.gameState

            if (state != null)
                Grid(game.value) { x, y -> onCellClick(x, y) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button to toggle the target state
            GameButton("Target $target") {
                game.value = game.value.setTargetMode(!game.value.target)
                target = if (game.value.target) "On" else "Off"
            }

            Spacer(modifier = Modifier.width(padding))

            // Main action button
            GameButton("Update") {
                game.value = game.value.refresh()
            }
        }
    }
}

/** Composable button with auto-sizing text */
@Composable
fun GameButton(label: String, onClick: () -> Unit) {
    Button(
        colors = buttonColors(
            containerColor = BUTTON_MAIN_COLOR,
            contentColor = BUTTON_CONTENT_COLOR
        ),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp)
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
fun Grid(
    game: Game,
    onCellClick: (x: Int, y: Int) -> Unit
) {
    val board: Board = game.gameState?.board ?: return
    val side = board.side
    val target = game.target
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BOARD_SIDE_COLOR, shape = RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(side) { y ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(side) { x ->
                        val coordinate = Coordinate(x+1, y+1)
                        val cellValue = board[coordinate]
                        val isTargetCell = target && game.getAvailablePlays().contains(coordinate)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BOARD_MAIN_COLOR)
                                .clickable { onCellClick(x+1, y+1) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellValue != null) {
                                when (cellValue) {
                                    PieceType.BLACK -> Text(
                                        text = "⚫",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp
                                    )
                                    PieceType.WHITE -> Text(
                                        text = "⚪",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp
                                    )
                                }
                            }
                            else if (isTargetCell) {
                                Text(
                                    text = "CAN",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
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

@Composable
@Preview
fun RowWithColumnDemo() {
    Column(
        modifier = Modifier
            .background(BOARD_BACKGROUND_COLOR)
            .padding(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GameButton("Teste") {}

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button to toggle the target state
            GameButton("Target") { }

            Spacer(modifier = Modifier.width(padding))

            // Main action button
            GameButton("PLAY") { /* button action */ }
        }
    }
}
