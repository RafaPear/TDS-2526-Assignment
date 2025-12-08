package pt.isel.reversi.app

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.TextUnit
import pt.isel.reversi.app.pages.MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ReversiException


class ReversiScope(val appState: MutableState<AppState>)

fun ReversiScope.setError(error: ReversiException) = appState.setError(error)
fun ReversiScope.getCurrentState() = appState.value
fun ReversiScope.getAudioPool() = appState.getStateAudioPool()
fun ReversiScope.play(soundName: String) = getAudioPool().play(soundName)
fun ReversiScope.setAppState(
    game: Game = getCurrentState().game,
    page: Page = getCurrentState().page,
    error: Exception? = null,
) = appState.setAppState(game, page, error)

fun ReversiScope.updateAppState(update: AppState.() -> AppState) {
    val currentState = getCurrentState()
    appState.value = currentState.update()
}
fun ReversiScope.getTheme() = getCurrentState().theme

@Composable
fun ReversiScope.ReversiText(
    text: String,
    color: Color = appState.value.theme.textColor,
    fontSize: TextUnit = TextUnit.Unspecified,
    autoSize: TextAutoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        autoSize = autoSize,
        modifier = modifier,
    )
}

@Composable
fun ReversiScope.ReversiButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val theme = getTheme()
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = theme.primaryColor,
        )
    ) {
        ReversiText(
            text = text,
            color = theme.textColor,
            autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
        )
    }
}

@Composable
fun ReversiScope.ReversiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
) {
    val theme = getTheme()
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = theme.primaryColor,
            unfocusedIndicatorColor = theme.textColor.copy(0.3f),
            cursorColor = theme.primaryColor,
            focusedTextColor = theme.textColor,
            unfocusedTextColor = theme.textColor,
            focusedLabelColor = theme.primaryColor,
            unfocusedLabelColor = theme.textColor
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
            }
        )
    )
}

