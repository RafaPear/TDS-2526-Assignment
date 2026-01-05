package pt.isel.reversi.app.pages.lobby.lobbyViews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ReversiText

const val EMPTY_LOBBY_TAG = "empty_lobby_view"
const val EMPTY_LOBBY_ICON_TAG = "empty_lobby_icon"
const val EMPTY_LOBBY_TEXT_TAG = "empty_lobby_text"
const val TEXT_EMPTY_LOBBY = "Nenhum jogo guardado"

@Composable
fun Empty(reversiScope: ReversiScope, buttonRefresh: @Composable () -> Unit = {}) {
    with(reversiScope) {
        Column(
            modifier = Modifier.testTag(EMPTY_LOBBY_TAG)
        ) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                modifier = Modifier.size(80.dp).testTag(EMPTY_LOBBY_ICON_TAG),
                tint = Color.White.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            ReversiText(
                TEXT_EMPTY_LOBBY,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifiew = Modifier.testTag(EMPTY_LOBBY_TEXT_TAG)
            )
            Spacer(Modifier.height(8.dp))
            ReversiText(
                "Comece um novo jogo",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            buttonRefresh()
        }
    }
}
