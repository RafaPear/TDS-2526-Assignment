package pt.isel.reversi.app.pages.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import pt.isel.reversi.app.ReversiButton
import pt.isel.reversi.app.ReversiScope

/** Composable button with auto-sizing text */
@Composable
fun ReversiScope.TargetButton(target: Boolean, modifier: Modifier = Modifier, freeze: Boolean, onClick: () -> Unit) {
    val targetText = if (target) "ON" else "OFF"

    ReversiButton(
        text = "Target $targetText",
        modifier = modifier.testTag(tag = testTagTargetButtons(target)),
        onClick = onClick,
        enabled = !freeze,
    )
}