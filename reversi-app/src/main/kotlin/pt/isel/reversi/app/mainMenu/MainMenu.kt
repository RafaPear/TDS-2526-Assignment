package pt.isel.reversi.app.mainMenu

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
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.getStateAudioPool
import pt.isel.reversi.app.state.setPage
import pt.isel.reversi.utils.LOGGER

val MAIN_MENU_PADDING = 20.dp

val MAIN_MENU_AUTO_SIZE_BUTTON_TEXT = TextAutoSize.StepBased(
    minFontSize = 10.sp,
    maxFontSize = 30.sp
)

val MAIN_MENU_BUTTON_SPACER = 30.dp

val MAIN_MENU_AUTO_SIZE_TITLE_TEXT = TextAutoSize.StepBased(
    minFontSize = 30.sp,
    maxFontSize = 60.sp
)

@Composable
fun MainMenu(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    LaunchedEffect(appState.value.page) {
        val audioPool = getStateAudioPool(appState)
        if (!audioPool.isPlaying(BACKGROUND_MUSIC)) {
            LOGGER.info("Playing background music")
            audioPool.stopAll()
            audioPool.play(BACKGROUND_MUSIC)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MAIN_MENU_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Título
        Text(
            text = "Reversi",
            autoSize = MAIN_MENU_AUTO_SIZE_TITLE_TEXT,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(MAIN_MENU_PADDING))

        Button(
            modifier = modifier,
            onClick = { appState.value = setPage(appState, Page.NEW_GAME) },
        ) {
            Text(
                text = "Novo Jogo",
                textAlign = TextAlign.Center,
                autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
            )
        }

        Spacer(Modifier.height(MAIN_MENU_BUTTON_SPACER))

        Button(
            modifier = modifier,
            onClick = { appState.value = setPage(appState, Page.JOIN_GAME) },
        ) {
            Text(
                text = "Entrar em Jogo",
                textAlign = TextAlign.Center,
                autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
            )
        }

        Spacer(Modifier.height(MAIN_MENU_BUTTON_SPACER))

        Button(
            modifier = modifier,
            onClick = { appState.value = setPage(appState, Page.SETTINGS) },
        ) {
            Text(
                text = "Definições",
                textAlign = TextAlign.Center,
                autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
            )
        }

        Spacer(Modifier.height(MAIN_MENU_BUTTON_SPACER))

        Button(
            modifier = modifier,
            onClick = { appState.value = setPage(appState, Page.ABOUT) },
        ) {
            Text(
                text = "Sobre",
                textAlign = TextAlign.Center,
                autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
            )
        }
    }
}



