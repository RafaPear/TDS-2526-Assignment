package pt.isel.reversi.app.pages.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.app.state.ReversiButton
import pt.isel.reversi.app.app.state.ReversiScope
import pt.isel.reversi.app.app.state.ReversiText
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.app.utils.PreviousPage
import pt.isel.reversi.utils.TRACKER

val MAIN_MENU_AUTO_SIZE_BUTTON_TEXT =
    TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 24.sp)

/**
 * Main menu screen showing navigation options.
 * Handles menu audio playback and navigation callbacks to other pages.
 *
 * @param viewModel View model handling menu audio and error state.
 * @param modifier Modifier for layout customization.
 * @param onLeave Callback invoked when returning to the previous screen.
 */
@Composable
fun ReversiScope.MainMenu(
    viewModel: MainMenuViewModel,
    modifier: Modifier = Modifier,
    onLeave: () -> Unit,
) {
    TRACKER.trackPageEnter(category = Page.MAIN_MENU)
    //TODO: Rever esta corroutina -- Porque? (rafa)
    LaunchedEffect(appState.pagesState.page) {
        viewModel.playMenuAudio()
    }

    ScaffoldView(
        setError = { error, type -> viewModel.setError(error, type) },
        error = viewModel.error,
        isLoading = viewModel.uiState.value.screenState.isLoading,
        previousPageContent = { PreviousPage { onLeave() } },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            AnimatedBackground()

            Column(
                modifier = modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReversiText(text = "REVERSI", fontWeight = FontWeight.Black, fontSize = 80.sp)
                Spacer(Modifier.height(40.dp))

                Column(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReversiButton("Novo Jogo") { viewModel.setPage(Page.NEW_GAME) }
                    ReversiButton("Lobby") { viewModel.setPage(Page.LOBBY) }
                    ReversiButton("Definições") { viewModel.setPage(Page.SETTINGS) }
                    ReversiButton("Sobre") { viewModel.setPage(Page.ABOUT) }
                }
            }
        }
    }
}