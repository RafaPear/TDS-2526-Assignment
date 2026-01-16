package pt.isel.reversi.app.pages.aboutPage

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.app.state.ReversiScope
import pt.isel.reversi.app.app.state.ReversiText
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.app.utils.PreviousPage
import pt.isel.reversi.utils.TRACKER


/**
 * Simple about page presenting project and authorship information.
 *
 * @param viewModel View model providing screen state and error handling.
 * @param modifier Optional modifier to adjust layout in previews or reuse.
 * @param onLeave Callback invoked when navigating back.
 */
@Composable
fun ReversiScope.AboutPage(viewModel: AboutPageViewModel, modifier: Modifier = Modifier, onLeave: () -> Unit) {
    TRACKER.trackPageEnter(category = Page.ABOUT)
    ScaffoldView(
        setError = { error, type -> viewModel.setError(error, type) },
        error = viewModel.error,
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
            ReversiText(" - Rafael Pereira - 52880", color = appState.theme.textColor)
            ReversiText(" - Ian Frunze - NUMERO", color = appState.theme.textColor)
            ReversiText(" - Tito Silva - A53118", color = appState.theme.textColor)
            ReversiText("Versão: DEV Build", color = appState.theme.textColor)

        }
    }
}
