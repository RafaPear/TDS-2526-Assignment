package pt.isel.reversi.app.pages.lobby.lobbyViews.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Icon button for refreshing the lobby game list.
 *
 * @param onClick Callback invoked when the refresh button is clicked.
 */
@Composable
fun RefreshButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Refresh",
            tint = Color.White.copy(alpha = 0.9f)
        )
    }
}