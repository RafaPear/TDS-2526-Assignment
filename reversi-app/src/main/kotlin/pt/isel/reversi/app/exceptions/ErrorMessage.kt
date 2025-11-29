package pt.isel.reversi.app.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        ErrorType.INFO     -> {
            LOGGER.info("${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        ErrorType.WARNING  -> {
            LOGGER.warning("${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        ErrorType.ERROR    -> {
            LOGGER.severe("${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        ErrorType.CRITICAL -> {
            LOGGER.severe("Critical ${appState.value.error?.message}")
            ToastMessage(appState, modifier)
        }

        null               -> return
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = appState.value.error?.message ?: return@Box,
            color = Color.White,
            modifier = Modifier
                .background(Color.Red, shape = RoundedCornerShape(20.dp))
                .padding(8.dp)
        )
    }

    LaunchedEffect(appState.value.error?.message) {
        if (appState.value.error?.message == null) return@LaunchedEffect
        // async wait for 2 seconds, more light than Thread.sleep
        delay(2000)
        appState.value = setError(appState, error = null)
    }
}
