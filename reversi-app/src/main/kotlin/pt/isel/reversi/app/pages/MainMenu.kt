package pt.isel.reversi.app.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.BACKGROUND_MUSIC
import pt.isel.reversi.app.PRIMARY
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.pages.game.TEXT_COLOR
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setPage
import pt.isel.reversi.utils.LOGGER

val MAIN_MENU_PADDING = 20.dp

val MAIN_MENU_AUTO_SIZE_BUTTON_TEXT = TextAutoSize.StepBased(
    minFontSize = 10.sp, maxFontSize = 30.sp
)

val MAIN_MENU_BUTTON_SPACER = 30.dp

val MAIN_MENU_AUTO_SIZE_TITLE_TEXT = TextAutoSize.StepBased(
    minFontSize = 30.sp, maxFontSize = 60.sp
)

@Composable
fun MainMenu(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    LaunchedEffect(appState.value.page) {
        val audioPool = appState.getStateAudioPool()
        if (!audioPool.isPlaying(BACKGROUND_MUSIC)) {
            LOGGER.info("Playing background music")
            audioPool.stopAll()
            audioPool.play(BACKGROUND_MUSIC)
        }
    }
    ScaffoldView(
        appState = appState,
        previousPageContent = { /* No previous page */ },
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(MAIN_MENU_PADDING),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Título
            Text(
                text = "Reversi",
                color = TEXT_COLOR,
                autoSize = MAIN_MENU_AUTO_SIZE_TITLE_TEXT,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(MAIN_MENU_PADDING))

            MainMenuButton(text = "Novo Jogo") {
                appState.setPage(Page.NEW_GAME)
            }

            Spacer(Modifier.height(MAIN_MENU_BUTTON_SPACER))

            MainMenuButton(text = "Lobby") {
                appState.setPage(Page.LOBBY)
            }

            Spacer(Modifier.height(MAIN_MENU_BUTTON_SPACER))

            MainMenuButton(text = "Definições") {
                appState.setPage(Page.SETTINGS)
            }

            Spacer(Modifier.height(MAIN_MENU_BUTTON_SPACER))

            MainMenuButton(text = "Sobre") {
                appState.setPage(Page.ABOUT)
            }
        }
    }
}

@Composable
fun MainMenuButton(
    text: String, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = PRIMARY
        )
    ) {
        Text(
            text = text, color = TEXT_COLOR, textAlign = TextAlign.Center, autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
        )
    }
}
