package pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.utils

import androidx.compose.runtime.Composable
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ReversiText
import pt.isel.reversi.app.ReversiTextField

@Composable
fun ReversiScope.Search(search: String, onValueChange: (String) -> Unit) {
    ReversiTextField(
        value = search,
        singleLine = true,
        onValueChange = { onValueChange(it) },
        label = { ReversiText("Procure um jogo...") },
    )
}