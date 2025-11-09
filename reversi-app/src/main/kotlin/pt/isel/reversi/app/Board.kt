package pt.isel.reversi.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


val PURPLE = Color(0xFF800080)
val GRID_SIZE = 500.dp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
    ) {
        Board()
    }
}


@Preview
@Composable
fun Board() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) { Grid() }
}

@Composable
fun Grid() {
    Box(
        modifier = Modifier
            .background(color = PURPLE)
            .size(GRID_SIZE),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .size(GRID_SIZE),
        ) {
            Canvas(modifier = Modifier.size(GRID_SIZE)) {
                drawGrid(8, this)
            }
        }
    }
}


fun drawGrid(side: Int, drawScope: DrawScope) = with(drawScope) {
    val padding = (GRID_SIZE / side).toPx()

    for (x in 1 until side) {
        drawLine(
            color = Color.Black,
            strokeWidth = 4f,
            start = Offset(x = x * padding, y = 0f),
            end = Offset(x = x * padding, y = size.height),
        )
    }

    for (y in 1 until side) {
        drawLine(
            color = Color.Black,
            strokeWidth = 4f,
            start = Offset(x = 0f, y = y * padding),
            end = Offset(x = size.width, y = y * padding),
        )
    }
}