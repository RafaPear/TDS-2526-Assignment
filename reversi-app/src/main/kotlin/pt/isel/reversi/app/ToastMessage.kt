package pt.isel.reversi.app

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

@Composable
fun ToastMessage(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = appState.value.toastMessage ?: return@Box,
            color = Color.White,
            modifier = Modifier
                .background(Color.Red, shape = RoundedCornerShape(20.dp))
                .padding(8.dp)
        )
    }

    LaunchedEffect(appState.value.toastMessage) {
        if (appState.value.toastMessage == null) return@LaunchedEffect
        // async wait for 2 seconds, more light than Thread.sleep
        delay(2000)
        appState.value = setToastMessage(appState, message = null)
    }
}
