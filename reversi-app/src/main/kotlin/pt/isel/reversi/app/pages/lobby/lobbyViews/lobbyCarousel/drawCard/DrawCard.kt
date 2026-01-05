package pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.pages.game.utils.DrawBoard
import pt.isel.reversi.app.pages.lobby.CARD_BG
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.CardStatus
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType

fun cardTestTag(gameId: String) = "game_card_$gameId"

fun headerBadgeTestTag(gameId: String) = "header_badge_$gameId"

fun scorePanelTestTag(gameId: String) = "score_panel_$gameId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCard(
    game: Game,
    enabled: Boolean,
    cardData: CardStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val name = game.currGameName ?: return
    val state = game.gameState ?: return

    val statusText = cardData.text
    val statusColor = cardData.color

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.shadow(16.dp, RoundedCornerShape(24.dp))
            .testTag(cardTestTag(name)),
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
            HeaderBadge(statusText, statusColor, name)

            DrawBoard(
                false,
                state,
                modifier = Modifier.weight(4f),
                true,
                {emptyList()},
                {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            ScorePanel(Modifier.testTag(scorePanelTestTag(name)), state.board)
        }
    }
}


@Composable
private fun HeaderBadge(statusText: String, statusColor: Color, name: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag(headerBadgeTestTag(name)),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        println(name)
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
}

@Composable
private fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
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
private fun ScorePanel(modifier: Modifier, board: Board) {
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
private fun ScoreItem(type: PieceType, score: Int) {
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