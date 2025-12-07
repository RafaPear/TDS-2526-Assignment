package pt.isel.reversi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.pages.lobby.PRIMARY


@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box (
        Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = false) {}
    ){
        Column(
            modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = PRIMARY, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("A carregar jogos...", color = Color.White, fontSize = 18.sp)
        }
    }
}