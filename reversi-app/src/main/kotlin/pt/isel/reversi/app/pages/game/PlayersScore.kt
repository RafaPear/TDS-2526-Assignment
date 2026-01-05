package pt.isel.reversi.app.pages.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ReversiText
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.storage.GameState

@Composable
fun ReversiScope.TextPlayersScore(
    state: GameState?,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ReversiText(
            "Jogadores",
            autoSize = TextAutoSize.StepBased(maxFontSize = 50.sp),
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(16.dp))

        if (state == null) {
            ReversiText(
                "Sem jogo ativo",
                autoSize = TextAutoSize.StepBased(maxFontSize = 50.sp),
                fontStyle = FontStyle.Italic,
            )
        } else {
            val players = listOf(
                Player(PieceType.BLACK, state.board.totalBlackPieces),
                Player(PieceType.WHITE, state.board.totalWhitePieces)
            )

            players.forEach { player ->
                val isTurn = player.type != state.lastPlayer

                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(if (player.type == PieceType.BLACK) "⚫ " else "⚪ ")
                    }
                    withStyle(style = SpanStyle(fontWeight = if (isTurn) FontWeight.Bold else FontWeight.Normal)) {
                        append("Peças: ${player.points} ")
                    }
                    withStyle(style = SpanStyle(color = if (isTurn) Color.Green else Color.Unspecified)) {
                        append(if (isTurn) "←" else "\u2007\u2007") // "\u2007 é para por um espaço vazio fixo"
                    }
                }

                ReversiText(
                    text = annotatedText.toString(),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 10.sp,
                        maxFontSize = 50.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTagPlayerScore(player)),
                )
            }

            state.winner?.let {
                ReversiText(
                    "Vencedor: ${it.type}",
                    color = Color.Green,
                    autoSize = TextAutoSize.StepBased(),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}


