package pt.isel.reversi.app.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.*
import pt.isel.reversi.app.gameAudio.loadGameAudioPool
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.setAppState

@Composable
private fun ReversiScope.SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Título da Secção com cor primária e divisor
        ReversiText(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = getTheme().primaryColor
        )
        HorizontalDivider(
            color = getTheme().textColor.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        // Conteúdo da secção
        content()
    }
}

@Composable
fun SettingsPage(appState: MutableState<AppState>) {
    // Aceder ao tema atual e pool de áudio via Scope
    val currentTheme = appState.value.theme
    val audioPool = appState.value.audioPool

    // Estado do Volume
    var volume by remember { mutableStateOf(audioPool.getMasterVolume() ?: 0f) }

    // Estado do Menu de Temas
    var themeMenuExpanded by remember { mutableStateOf(false) }

    val availableThemes = AppThemes.entries.map { it.appTheme }

    ScaffoldView(
        appState = appState,
        title = "Definições",
        previousPageContent = {
            // Volta para a página anterior guardada no estado
            PreviousPage { appState.setAppState(page = getCurrentState().backPage) }
        }
    ) { padding ->

        // BOX para centralização responsiva
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 24.dp)
                    .widthIn(max = 500.dp) // O segredo para o design Desktop profissional
                    .fillMaxWidth(0.9f)
                    .verticalScroll(rememberScrollState()), // Permite scroll em ecrãs pequenos
                verticalArrangement = Arrangement.spacedBy(32.dp), // Espaço maior entre secções
                horizontalAlignment = Alignment.Start
            ) {

                // --- SECÇÃO 1: ÁUDIO ---
                SettingsSection(title = "Áudio e Som") {

                    val volumePercent = volumeDbToPercent(volume, -20f, 0f)
                    val volumeLabel = if (volume <= -20f) "Mudo" else "$volumePercent%"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReversiText("Volume Geral", fontSize = 16.sp)
                        ReversiText(volumeLabel, fontWeight = FontWeight.Bold, color = currentTheme.primaryColor)
                    }

                    Slider(
                        value = volume,
                        valueRange = -20f..0f,
                        onValueChange = { newVolume ->
                            volume = newVolume
                            if (volume <= -20f) {
                                audioPool.mute(true)
                            } else {
                                audioPool.mute(false)
                                audioPool.setMasterVolume(volume)
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.primaryColor,
                            activeTrackColor = currentTheme.primaryColor,
                            inactiveTrackColor = currentTheme.primaryColor.copy(alpha = 0.2f)
                        )
                    )
                }

                // --- SECÇÃO 2: PERSONALIZAÇÃO (TEMAS) ---
                SettingsSection(title = "Aspeto Visual") {
                    ReversiText(
                        "Tema da Aplicação",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // O Dropdown de Temas
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { themeMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, currentTheme.textColor.copy(0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = currentTheme.secondaryColor.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mostra o nome do tema atual
                                ReversiText(currentTheme.name) // Assume que o tema tem uma propriedade .name
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Trocar tema",
                                    tint = currentTheme.primaryColor
                                )
                            }
                        }

                        ReversiDropDownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = { themeMenuExpanded = false }
                        ) {
                            // Itera sobre os teus temas reais aqui
                            // Exemplo genérico:
                            availableThemes.forEach { theme ->
                                ReversiDropdownMenuItem(
                                    text = theme.name,
                                    onClick = {
                                        audioPool.destroy()
                                        appState.setAppState(theme = theme, audioPool = loadGameAudioPool(theme))
                                        themeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ReversiText(
                        "A alteração do tema é aplicada imediatamente.",
                        fontSize = 12.sp,
                        color = currentTheme.textColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}