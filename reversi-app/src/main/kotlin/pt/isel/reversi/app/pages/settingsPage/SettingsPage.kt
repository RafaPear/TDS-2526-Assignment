package pt.isel.reversi.app.pages.settingsPage

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.isel.reversi.app.*
import pt.isel.reversi.core.CoreConfig
import pt.isel.reversi.core.loadCoreConfig
import pt.isel.reversi.core.storage.GameStorageType
import pt.isel.reversi.utils.TRACKER

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
 * @param viewModel The ViewModel managing the settings state and logic.
 * @param onLeave Callback invoked when leaving the settings page.
 */
@Composable
fun ReversiScope.SettingsPage(
    viewModel: SettingsViewModel,
    onLeave: () -> Unit
) {
    TRACKER.trackRecomposition()

    val draftPlayerName = remember { mutableStateOf(appState.playerName) }
    val draftTheme = remember { mutableStateOf(appState.theme) }

    val draftCoreConfig = remember { mutableStateOf(loadCoreConfig()) }
    var currentVol by remember {
        val masterVol = appState.audioPool.getMasterVolume()
        val isMuted = appState.audioPool.isPoolMuted()
        val min = appState.audioPool.getMasterVolumeRange()?.first

        if (isMuted) mutableStateOf(min ?: -20f)
        else mutableStateOf(masterVol ?: 0f)
    }

    ScaffoldView(
        setError = { error -> viewModel.setError(error) },
        error = viewModel.uiState.value.screenState.error,
        isLoading = viewModel.uiState.value.screenState.isLoading,
        title = "Definições",
        previousPageContent = {
            PreviousPage { onLeave() }
        }
    ) { padding ->
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState(0)

        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {

            // Scroll Bar for Desktop
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = scrollState
                ),
                style = ScrollbarStyle(
                    minimalHeight = 16.dp,
                    thickness = 8.dp,
                    shape = RoundedCornerShape(4.dp),
                    hoverDurationMillis = 300,
                    unhoverColor = getTheme().primaryColor.copy(alpha = 0.12f),
                    hoverColor = getTheme().primaryColor.copy(alpha = 0.24f)
                )
            )

            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(0.9f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                GameSection(
                    playerName = draftPlayerName.value,
                    onValueChange = {
                        draftPlayerName.value = it
                    }
                )

                CoreConfigSection(
                    coreConfig = draftCoreConfig.value,
                    onConfigChange = { draftCoreConfig.value = it }
                )

                AudioSection(
                    currentVol = currentVol,
                    onVolumeChange = {
                        currentVol = it
                    },
                )

                AppearanceSection(
                    theme = draftTheme.value,
                    appTheme = appState.theme
                ) { newTheme ->
                    draftTheme.value = newTheme
                }

                // Apply button
                ApplyButton {
                    scope.launch {
                        TRACKER.trackFunctionCall(details = "Apply settings clicked")
                        viewModel.applySettings(
                            oldTheme = appState.theme,
                            newName = draftPlayerName.value,
                            newTheme = draftTheme.value,
                            draftCoreConfig = draftCoreConfig.value,
                            volume = currentVol
                        )
                    }
                }
            }
        }
    }
}

//TODO: Rever esta fun com as novas alteracoes
//// Settings application logic (simplified to avoid unresolved references)
//private suspend fun applySettings(
//    appState: AppState,
//    draft: AppState,
//    draftCoreConfig: CoreConfig,
//    volume: Float
//): AppState {
//    setLoading(appState, true)
//
//    try {
//
//        // check if storage type changed and test connection if needed
//        val currentCoreConfig = loadCoreConfig()
//        if (currentCoreConfig != draftCoreConfig) {
//            LOGGER.info("Storage type changed from ${currentCoreConfig.gameStorageType} to ${draftCoreConfig.gameStorageType}, testing connectivity...")
//            val exception = runStorageHealthCheck(testConf = draftCoreConfig, save = true)
//            if (exception != null) {
//                setError(appState, exception, ErrorType.WARNING)
//                LOGGER.severe("Storage type change failed: ${exception.message}")
//            }
//            else {
//                saveCoreConfig(draftCoreConfig)
//                LOGGER.info("Core config saved: storageType=${draftCoreConfig.gameStorageType}")
//            }
//        }
//
//        // Load audio pool for the selected theme and merge into current
//        val current = appState
//        val oldTheme = current.theme.value
//        val playingAudios = current.audioPool.value.getPlayingAudios()
//
//        val loadedAudioPool = loadGameAudioPool(draft.theme.value) { err ->
//            setError(appState, err)
//        }
//        current.audioPool.value.merge(loadedAudioPool)
//
//        // Apply audio volume
//        parseVolume(volume, current)
//
//        // Apply theme and player name
//        setAppState(
//            appState,
//            game = current.game.value,
//            playerName = draft.playerName.value,
//            theme = draft.theme.value
//        )
//
//        // Resume previously playing theme-related audios
//        for (audio in playingAudios) {
//            val audioToPlay = when (audio) {
//                oldTheme.backgroundMusic -> draft.theme.value.backgroundMusic
//                oldTheme.gameMusic -> draft.theme.value.gameMusic
//                else -> null
//            }
//            if (audioToPlay != null && !current.audioPool.value.isPlaying(audioToPlay)) {
//                current.audioPool.value.play(audioToPlay)
//                LOGGER.info("Resuming audio: $audioToPlay")
//            }
//        }
//
//        val loadedAudios = current.audioPool.value.pool.map { it.id }
//        LOGGER.info("Loaded audios after applying settings: $loadedAudios")
//
//        // Small delay for UX
//        delay(100)
//    } catch (e: Exception) {
//        LOGGER.severe("Failed to apply settings: ${e.message}")
//    } finally {
//        setLoading(appState, false)
//    }
//
//    return appState
//}


@Composable
private fun ReversiScope.GameSection(playerName: String?, onValueChange: (String) -> Unit) {
    SettingsSection(title = "Jogo") {
        ReversiTextField(
            value = playerName ?: "",
            onValueChange = { onValueChange(it) },
            label = { ReversiText("Nome do Jogador") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReversiScope.CoreConfigSection(
    coreConfig: CoreConfig,
    onConfigChange: (CoreConfig) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsSection(title = "Configuração do Jogo") {
        // Storage Type Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReversiText("Tipo de Armazenamento: ${coreConfig.gameStorageType.name}")
                }
            }

            ReversiDropDownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                GameStorageType.entries.forEach { storageType ->
                    ReversiDropdownMenuItem(
                        text = storageType.name,
                        onClick = {
                            onConfigChange(coreConfig.copy(gameStorageType = storageType))
                            expanded = false
                        }
                    )
                }
            }
        }

        // File Storage Path (only show when FILE_STORAGE is selected)
        if (coreConfig.gameStorageType == GameStorageType.FILE_STORAGE) {
            ReversiTextField(
                value = coreConfig.savesPath,
                onValueChange = { onConfigChange(coreConfig.copy(savesPath = it)) },
                label = { ReversiText("Caminho das Gravações") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Database Settings (only show when DATABASE_STORAGE is selected)
        if (coreConfig.gameStorageType == GameStorageType.DATABASE_STORAGE) {
            ReversiTextField(
                value = coreConfig.dbURI,
                onValueChange = { onConfigChange(coreConfig.copy(dbURI = it)) },
                label = { ReversiText("URI do Banco de Dados") },
                modifier = Modifier.fillMaxWidth()
            )

            ReversiTextField(
                value = coreConfig.dbPort.toString(),
                onValueChange = {
                    val newPort = it.toIntOrNull()
                    if (newPort != null && newPort > 0) {
                        onConfigChange(coreConfig.copy(dbPort = newPort))
                    }
                },
                label = { ReversiText("Porta do Banco de Dados") },
                modifier = Modifier.fillMaxWidth()
            )

            ReversiTextField(
                value = coreConfig.dbName,
                onValueChange = { onConfigChange(coreConfig.copy(dbName = it)) },
                label = { ReversiText("Nome do Banco de Dados") },
                modifier = Modifier.fillMaxWidth()
            )

            ReversiTextField(
                value = coreConfig.dbUser,
                onValueChange = { onConfigChange(coreConfig.copy(dbUser = it)) },
                label = { ReversiText("Usuário do Banco de Dados") },
                modifier = Modifier.fillMaxWidth()
            )

            ReversiTextField(
                value = coreConfig.dbPassword,
                onValueChange = { onConfigChange(coreConfig.copy(dbPassword = it)) },
                label = { ReversiText("Senha do Banco de Dados") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReversiScope.AudioSection(
    currentVol: Float,
    onVolumeChange: (Float) -> Unit
) {
    SettingsSection(title = "Áudio") {
        val (minVol, maxVol) = appState.audioPool.getMasterVolumeRange() ?: (-20f to 0f)
        val percent = (((currentVol - minVol) / (maxVol - minVol)) * 100).toInt().coerceIn(0, 100)
        val volumeLabel = if (currentVol <= minVol) "Mudo" else "$percent%"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReversiText("Volume Geral", fontSize = 16.sp)
            ReversiText(volumeLabel, fontWeight = FontWeight.Bold)
        }

        Slider(
            value = currentVol,
            valueRange = minVol..maxVol,
            onValueChange = onVolumeChange,
            colors = SliderDefaults.colors(
                thumbColor = appState.theme.primaryColor,
                activeTrackColor = appState.theme.primaryColor,
                inactiveTrackColor = appState.theme.textColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ReversiScope.AppearanceSection(
    theme: AppTheme,
    appTheme: AppTheme,
    onClick: (AppTheme) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsSection(title = "Aspeto Visual") {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReversiText(theme.name)
                    Icon(Icons.Default.Palette, null, tint = appTheme.primaryColor)
                }
            }

            ReversiDropDownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AppThemes.entries.forEach { entry ->
                    ReversiDropdownMenuItem(
                        text = entry.appTheme.name,
                        onClick = {
                            onClick(entry.appTheme)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReversiScope.ApplyButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        ReversiButton(text = "Aplicar", onClick = onClick)
    }
}

// applySettings left for later refinement
