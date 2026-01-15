package pt.isel.reversi.app.pages.winnerPage

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.state.ReversiScope
import pt.isel.reversi.app.state.ReversiText
import pt.isel.reversi.app.utils.PreviousPage

@Composable
fun ReversiScope.WinnerPage(
    viewModel: WinnerPageViewModel,
    modifier: Modifier = Modifier,
    onLeave: () -> Unit
) {
    val state = viewModel.uiState.value

    ScaffoldView(
        setError = { error, type -> viewModel.setError(error, type) },
        error = state.screenState.error,
        isLoading = state.screenState.isLoading,
        title = "Vencedor",
        previousPageContent = {
            PreviousPage { onLeave() }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues = padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(2f))

            val winner = state.winner

            if (winner != null) {
                ReversiText(
                    text = winner.name,
                    color = appState.theme.textColor,
                    fontSize = 40.sp
                )
                ReversiText(
                    text = "${winner.points} pontos.",
                    color = appState.theme.textColor,
                    fontSize = 40.sp
                )
            } else {
                ReversiText(
                    text = "Dados do vencedor indisponíveis.",
                    color = appState.theme.textColor
                )
            }

            Spacer(Modifier.weight(2f))
        }
    }

}