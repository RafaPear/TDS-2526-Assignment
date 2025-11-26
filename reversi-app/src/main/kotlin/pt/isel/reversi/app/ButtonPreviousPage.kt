package pt.isel.reversi.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

const val DEFAULT_SCALE = 2.0f
@Composable
fun PreviousPage(onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val offsetX by animateFloatAsState(
        targetValue = if (isHovered) -4f else 0f,
    )

    val scale by animateFloatAsState(
        targetValue = if (isHovered) DEFAULT_SCALE + 0.1f else DEFAULT_SCALE,
    )
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        modifier = Modifier
            .graphicsLayer {
                translationX = offsetX
                scaleX = scale
                scaleY = DEFAULT_SCALE
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onBack() },
        tint = Color.Black,
    )
}