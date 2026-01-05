package pt.isel.reversi.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.pages.MAIN_MENU_AUTO_SIZE_BUTTON_TEXT
import pt.isel.reversi.app.state.AppState


class ReversiScope(val appState: AppState)

fun ReversiScope.getCurrentState() = appState

fun ReversiScope.getTheme() = getCurrentState().theme

@Composable
fun ReversiScope.ReversiText(
    text: String,
    color: Color = appState.theme.textColor,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    fontStyle: FontStyle = FontStyle.Normal,
    textAlign: TextAlign = TextAlign.Start,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        autoSize = autoSize,
        modifier = modifier,
        fontWeight = fontWeight,
        maxLines = maxLines,
        softWrap = softWrap,
        fontStyle = fontStyle,
        textAlign = textAlign,
        overflow = overflow,
    )
}

@Composable
fun ReversiScope.ReversiButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    // Usamos uma percentagem para o shape ser proporcional ao tamanho do botão
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    border: BorderStroke? = null,
) {
    val theme = getTheme()
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = theme.primaryColor,
            disabledContainerColor = theme.primaryColor.copy(alpha = 0.5f)
        ),
        enabled = enabled,
        shape = shape,
        border = if (border == null) BorderStroke(width = 1.dp, color = theme.buttonTextColor) else border,
        // Adiciona um padding interno mínimo para o texto não tocar nos bordos
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ReversiText(
            text = text,
            color = theme.buttonTextColor,
            // O uso de autoSize já é uma excelente prática para adaptabilidade
            autoSize = MAIN_MENU_AUTO_SIZE_BUTTON_TEXT,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ReversiScope.ReversiDropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ReversiScope.() -> Unit,
) {
    val theme = getTheme()
    DropdownMenu(
        modifier = modifier.background(theme.backgroundColor),
        expanded = expanded,
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = onDismissRequest,
    ) {
        CompositionLocalProvider(LocalContentColor provides theme.textColor) {
            content()
        }
    }
}

@Composable
fun ReversiScope.ReversiDropdownMenuItem(
    text: String, // Simplificado para String para manter o estilo do tema
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val theme = getTheme()
    DropdownMenuItem(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).fillMaxWidth(),
        text = {
            ReversiText(
                text = text,
                color = theme.textColor,
                fontSize = 16.sp
            )
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = theme.textColor,
            trailingIconColor = theme.textColor,
            leadingIconColor = theme.textColor,
            disabledTextColor = theme.textColor.copy(alpha = 0.3f),
        ),
        enabled = enabled
    )
}

@Composable
fun ReversiScope.ReversiTextField(
    value: String = "",
    onValueChange: (String) -> Unit = {},
    placeholder: @Composable () -> Unit = {},
    label: @Composable () -> Unit = {},
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    enabled: Boolean = true,
) {
    val theme = getTheme()
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
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
        ),
        enabled = enabled
    )
}

