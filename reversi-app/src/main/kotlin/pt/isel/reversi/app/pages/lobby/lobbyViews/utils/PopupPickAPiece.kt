package pt.isel.reversi.app.pages.lobby.lobbyViews.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ReversiText
import pt.isel.reversi.core.board.PieceType


@Composable
fun ReversiScope.PopupPickAPiece(
    pieces: List<PieceType>,
    onPick: (PieceType) -> Unit,
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { })
                }
                .background(Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ReversiText(
                    text = "Escolha a sua peça",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    pieces.forEach { piece ->
                        val color = when (piece) {
                            PieceType.BLACK -> Color.Black
                            PieceType.WHITE -> Color.White
                        }
                        val borderColor = when (piece) {
                            PieceType.BLACK -> Color.White.copy(alpha = 0.3f)
                            PieceType.WHITE -> Color.Black.copy(alpha = 0.2f)
                        }

                        IconButton(
                            onClick = { onPick(piece) },
                            modifier = Modifier
                                .size(80.dp)
                                .background(color, CircleShape)
                                .border(2.dp, borderColor, CircleShape)
                        ) { }
                    }
                }
            }
        }
    }
}