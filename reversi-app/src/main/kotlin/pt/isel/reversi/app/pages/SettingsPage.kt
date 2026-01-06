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

/**
 * Section header composable for organizing settings into logical groups.
 * Displays a title and divider line with the section content below.
 *
 * @param title The section title/header.
 * @param content Lambda for the section's content composables.
 */
@Composable
private fun ReversiScope.SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReversiText(
            text = title,
            color = getTheme().primaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            color = getTheme().textColor.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        content()
    }
}

/**
 * Settings page displaying application configuration options.
 * Includes theme selection and audio volume control.
 *
 * @param appState Global application state for accessing and updating settings.
 */
@Composable
fun SettingsPage(appState: MutableState<AppState>) {
    val currentTheme = appState.value.theme
    val audioPool = appState.value.audioPool

    var volume by remember { mutableStateOf(audioPool.getMasterVolume() ?: 0f) }

    var themeMenuExpanded by remember { mutableStateOf(false) }

    val availableThemes = AppThemes.entries.map { it.appTheme }

    ScaffoldView(
        appState = appState,
        title = "Definições",
        previousPageContent = {
            PreviousPage { appState.setAppState(page = getCurrentState().backPage) }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 24.dp)
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(0.9f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.Start
            ) {

                // --- SECÇÃO 1: ÁUDIO ---
                SettingsSection(title = "Áudio") {

                    val volumePercent = volumeDbToPercent(volume, -20f, 0f)
                    val volumeLabel = if (volume <= -20f) "Mudo" else "$volumePercent%"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReversiText("Volume Geral", fontSize = 16.sp)
                        ReversiText(volumeLabel, color = currentTheme.primaryColor, fontWeight = FontWeight.Bold)
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

                SettingsSection(title = "Aspeto Visual") {
                    ReversiText(
                        "Tema da Aplicação",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

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
                                ReversiText(currentTheme.name)
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
                            availableThemes.forEach { theme ->
                                ReversiDropdownMenuItem(
                                    text = theme.name,
                                    onClick = {
                                        if (theme == currentTheme) {
                                            themeMenuExpanded = false
                                            return@ReversiDropdownMenuItem
                                        }
                                        audioPool.destroy()
                                        appState.setAppState(theme = theme, audioPool = loadGameAudioPool(theme))
                                        val newAudioPool = appState.value.audioPool
                                        if (volume <= -20f) {
                                            newAudioPool.mute(true)
                                        } else {
                                            newAudioPool.mute(false)
                                            newAudioPool.setMasterVolume(volume)
                                        }
                                        themeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ReversiText(
                        "A alteração do tema é aplicada imediatamente.",
                        color = currentTheme.textColor.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}