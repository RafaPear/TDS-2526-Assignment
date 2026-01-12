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
import pt.isel.reversi.app.*
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.utils.TRACKER

val MAIN_MENU_AUTO_SIZE_BUTTON_TEXT =
    TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 24.sp)


@Composable
fun ReversiScope.MainMenu(
    viewModel: MainMenuViewModel,
    modifier: Modifier = Modifier,
    setPage: (Page) -> Unit,
    onLeave: () -> Unit,
) {
    TRACKER.trackPageEnter()
    LaunchedEffect(appState.page) {
        val audioPool = getStateAudioPool(appState)
        val theme = appState.theme
        if (!audioPool.isPlaying(theme.backgroundMusic)) {
            audioPool.stopAll()
            audioPool.play(theme.backgroundMusic)
        }
    }

    ScaffoldView(
        setError = { error -> viewModel.setError(error) },
        error = viewModel.uiState.value.screenState.error,
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
                    ReversiButton("Novo Jogo") { setPage(Page.NEW_GAME) }
                    ReversiButton("Lobby") { setPage(Page.LOBBY) }
                    ReversiButton("Definições") { setPage(Page.SETTINGS) }
                    ReversiButton("Sobre") { setPage(Page.ABOUT) }
                }
            }
        }
    }
}