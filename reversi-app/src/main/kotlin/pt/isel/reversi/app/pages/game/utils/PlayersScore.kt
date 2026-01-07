package pt.isel.reversi.app.pages.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ReversiText
import pt.isel.reversi.app.getTheme
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.storage.GameState

/**
 * Displays the score card showing both players' piece counts and game status.
 * Highlights the current player's turn and displays the winner when the game ends.
 *
 * @param state Current game state, or null if no game is active.
 */
@Composable
fun ReversiScope.TextPlayersScore(
    state: GameState?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        ReversiText(
            text = "Placar",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        Spacer(Modifier.height(24.dp))

        if (state == null) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ReversiText(
                    "Sem jogo ativo",
                    fontStyle = FontStyle.Italic,
                )
            }
        } else {
            val players = state.playerNames.map { playerName ->
                val points = when (playerName.type) {
                    PieceType.BLACK -> state.board.totalBlackPieces
                    PieceType.WHITE -> state.board.totalWhitePieces
                }
                Player(playerName.type, points)
            }

            players.forEach { player ->
                val (type, points) = player
                val isTurn = type != state.lastPlayer
                val isWinner = state.winner?.type == type

                PlayerScoreRow(
                    type = type,
                    points = points,
                    isTurn = isTurn,
                    isWinner = isWinner,
                    modifier = Modifier.testTag(testTagPlayerScore(player))
                )

                Spacer(Modifier.height(8.dp))
            }

            // Mensagem de Final de Jogo
            AnimatedVisibility(visible = state.winner != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    ReversiText(
                        text = "Vencedor: ${state.winner?.type}",
                        color = Color.Green,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReversiScope.PlayerScoreRow(
    type: PieceType,
    points: Int,
    isTurn: Boolean,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isTurn) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .border(
                width = if (isTurn) 2.dp else 0.dp,
                color = if (isTurn) Color.Green.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Indicador visual da peça
            Surface(
                shape = CircleShape,
                color = if (type == PieceType.BLACK) getTheme().darkPieceColor else getTheme().lightPieceColor,
                border = BorderStroke(1.dp, getTheme().lightPieceColor),
                modifier = Modifier.size(24.dp)
            ) {}

            Spacer(Modifier.width(12.dp))

            val playerName = appState.game.gameState?.playerNames?.firstOrNull {
                it.type == type
            }?.name ?: type.name

            ReversiText(
                text = playerName,
                fontWeight = if (isTurn) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.alpha(if (isTurn || isWinner) 1f else 0.6f)
            )
        }

        ReversiText(
            text = "$points",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = if (isWinner) Color.Green else getTheme().textColor
        )
    }
}

