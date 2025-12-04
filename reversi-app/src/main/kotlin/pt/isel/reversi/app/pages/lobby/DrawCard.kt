package pt.isel.reversi.app.pages.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.pages.game.BOARD_BACKGROUND_COLOR
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType

private val PRIMARY = Color(0xFF1976D2)

private val BOARD_COLOR = Color(0xFF2E7D32)
private val BOARD_BORDER = Color(0xFF1B5E20)

private val CARD_BG = Color(0xFF1E1E1E)


@Composable
fun BoardPreview(board: Board, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp))
            .background(BOARD_BACKGROUND_COLOR)
            .padding(6.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            repeat(board.side) { y ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    repeat(board.side) { x ->
                        val piece = board[Coordinate(x + 1, y + 1)]
                        Cell(piece, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(piece: PieceType?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape = RoundedCornerShape(6.dp))
            .aspectRatio(1f)
            .background(BOARD_COLOR),
    ) {
        if (piece != null) {
            Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                val radius = size.minDimension / 2 * 0.7f
                val center = Offset(size.width / 2, size.height / 2)
                val color = if (piece == PieceType.BLACK) Color.Black else Color.White


                val sideColor = if (color == Color.White) Color(0xFFCFD8DC) else Color(0xFF37474F)
                drawCircle(sideColor, radius, center + Offset(1f, 1f))
                // Peça
                drawCircle(color.copy(alpha = .4f), radius * 0.95f, center)
                // Highlight
                drawCircle(
                    Color.White.copy(if (color == Color.White) 0.6f else 0.35f),
                    radius * 0.25f,
                    center - Offset(radius * 0.35f, radius * 0.35f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCard(
    game: Game,
    enabled: Boolean,
    statusData: GameStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val name = game.currGameName ?: return
    val state = game.gameState ?: return

    // Lógica corrigida dos estados
    val statusText = statusData.text
    val statusColor = statusData.color

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1E1E), Color(0xFF2D2D2D))
                )

            ).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StatusBadge(statusText, statusColor)
            }

            BoardPreview(
                board = state.board, modifier = Modifier.weight(4f).padding(vertical = 12.dp)
            )

            ScorePanel(Modifier, state.board)


//            if (state.players.isNotEmpty()) {// Botão
//                Button(
//                    onClick = onClick,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY),
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
//                    Spacer(Modifier.width(8.dp))
//                    Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
//                }
//            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp), color = color.copy(0.2f)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun ScorePanel(modifier: Modifier, board: Board) {
    Row(
        modifier = modifier.fillMaxWidth().background(Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreItem(PieceType.BLACK, board.totalBlackPieces)
        ScoreItem(PieceType.WHITE, board.totalWhitePieces)
    }
}

@Composable
fun ScoreItem(type: PieceType, score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(40.dp).background(
                if (type == PieceType.BLACK) Color.Black else Color.White, CircleShape
            ).border(
                2.dp, if (type == PieceType.BLACK) Color.White.copy(0.3f) else Color.Black.copy(0.2f), CircleShape
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = score.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White
        )
    }
}