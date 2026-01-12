package pt.isel.reversi.app.pages.aboutPage

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.PreviousPage
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.ReversiText
import pt.isel.reversi.app.ScaffoldView


/**
 * Simple about page presenting project and authorship information.
 *
 * @param appState Global state holder used for navigation and theming.
 * @param modifier Optional modifier to adjust layout in previews or reuse.
 */
@Composable
fun ReversiScope.AboutPage(viewModel: AboutPageViewModel, modifier: Modifier = Modifier, onLeave: () -> Unit) {

    ScaffoldView(
        setError = { error -> viewModel.setError(error) },
        error = viewModel.uiState.value.screenState.error,
        isLoading = viewModel.uiState.value.screenState.isLoading,
        title = "Sobre",
        previousPageContent = {
            PreviousPage { onLeave() }
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues = padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(height = 24.dp))
            ReversiText("Projeto Reversi desenvolvido no ISEL.", color = appState.theme.textColor)
            ReversiText("Autores: ", color = appState.theme.textColor)
            ReversiText(" - Rafael Pereira - NUMERO", color = appState.theme.textColor)
            ReversiText(" - Ian Frunze - NUMERO", color = appState.theme.textColor)
            ReversiText(" - Tito Silva - NUMERO", color = appState.theme.textColor)
            ReversiText("Versão: DEV Build", color = appState.theme.textColor)

        }
    }
}
