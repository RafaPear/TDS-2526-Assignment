package pt.isel.reversi.app.exceptions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.utils.LOGGER

/**
 * Composable that displays an error message based on the error type in the app state.
 * @param appState Mutable state of the application containing the current error information.
 * @param modifier Optional modifier for styling the composable.
 */
@Composable
fun ErrorMessage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    //TODO: Differentiate error types with different UI elements
    when (appState.value.error?.type) {
        ErrorType.INFO -> {
            LOGGER.info("${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        ErrorType.WARNING -> {
            LOGGER.warning("${appState.value.error?.message}")
            WarningMessage(appState, modifier)
        }

        ErrorType.ERROR -> {
            LOGGER.severe("${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        ErrorType.CRITICAL -> {
            LOGGER.severe("Critical ${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        null -> return
    }
}

@Composable
fun WarningMessage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val errorMessage = appState.value.error?.message ?: return

    val overlayColor = Color.Black.copy(alpha = 0.6f)
    val warningBackgroundColor = Color(0xFFFFCC80)
    val buttonBackgroundColor = Color(0xFFFFA000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayColor)
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                .background(warningBackgroundColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )


            Text(
                text = errorMessage,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    appState.value = setError(appState, error = null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBackgroundColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "OK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
/**
 * Composable that shows a toast message for errors.
 * The message is displayed for 2 seconds before being cleared.
 * @param appState Mutable state of the application containing the current error information.
 * @param modifier Optional modifier for styling the composable.
 */
@Composable
fun ToastMessage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val offsetY = remember { Animatable(-100f) }
    val error = appState.value.error
    val message = error?.message


    val slideDuration = 300
    val displayDuration = 2000L

    val infoBackgroundColor = Color(0xFFFF5722)

    val infoTextColor = Color.White.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = offsetY.value.toInt()) }
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(infoBackgroundColor)
                .height(IntrinsicSize.Min)
        ) {
            Text(
                text = message ?: return@Box,
                color = infoTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }

    LaunchedEffect(error, message) {
        if (error == null) return@LaunchedEffect

        offsetY.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = slideDuration)
        )
        delay(displayDuration)

        offsetY.animateTo(
            targetValue = -100f,
            animationSpec = tween(durationMillis = slideDuration)
        )
        appState.value = setError(appState, error = null)
    }
}
