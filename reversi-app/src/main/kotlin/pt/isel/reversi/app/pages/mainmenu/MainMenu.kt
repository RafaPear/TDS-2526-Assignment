package pt.isel.reversi.app.pages.mainmenu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ReversiButton
import pt.isel.reversi.app.ReversiText
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setPage

val MAIN_MENU_AUTO_SIZE_BUTTON_TEXT =
    TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 24.sp)


@Composable
fun MainMenu(
    appState: MutableState<AppState>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(appState.value.page) {
        val audioPool = appState.getStateAudioPool()
        val theme = appState.value.theme
        if (!audioPool.isPlaying(theme.backgroundMusic)) {
            audioPool.stopAll()
            audioPool.play(theme.backgroundMusic)
        }
    }

    ScaffoldView(appState = appState, previousPageContent = {}) {
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
                    ReversiButton("Novo Jogo") { appState.setPage(Page.NEW_GAME) }
                    ReversiButton("Lobby") { appState.setPage(Page.LOBBY) }
                    ReversiButton("Definições") { appState.setPage(Page.SETTINGS) }
                    ReversiButton("Sobre") { appState.setPage(Page.ABOUT) }
                }
            }
        }
    }
}