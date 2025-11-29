package pt.isel.reversi.app.gamePage

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Composable button with auto-sizing text */
@Composable
fun GameButton(label: String, modifier: Modifier = Modifier, freeze: Boolean, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        colors = buttonColors(
            containerColor = BUTTON_MAIN_COLOR,
            contentColor = BUTTON_CONTENT_COLOR
        ),
        enabled = !freeze,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased(
                minFontSize = BUTTON_MIN_FONT_SIZE,
                maxFontSize = BUTTON_MAX_FONT_SIZE
            ),
            color = BUTTON_TEXT_COLOR
        )
    }
}