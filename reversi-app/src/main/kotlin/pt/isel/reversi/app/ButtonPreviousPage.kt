package pt.isel.reversi.app

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

const val SCALE_MULTIPLIER = 2.0f

@Composable
fun PreviousPage(onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val offsetX by animateFloatAsState(
        targetValue = if (isHovered) -4f else 0f,
    )

    val iconSize by animateDpAsState(
        targetValue = if (isHovered) 28.dp else 24.dp,
    )

    Box(
        modifier = Modifier.padding(start = 12.dp)
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
            contentDescription = "Back",
            modifier = Modifier
                .size(iconSize * SCALE_MULTIPLIER)  // Anima o tamanho real
                .graphicsLayer {
                    translationX = offsetX
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onBack() },
            tint = Color.Black,
        )
    }
}