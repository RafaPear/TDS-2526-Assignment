package pt.isel.reversi.app.pages

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.*
import pt.isel.reversi.app.gameAudio.loadGameAudioPool
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.CoreConfig
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.loadCoreConfig
import pt.isel.reversi.core.loadGame
import pt.isel.reversi.core.saveCoreConfig
import pt.isel.reversi.core.storage.GameStorageType
import pt.isel.reversi.utils.LOGGER

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
fun SettingsPage(appState: AppState) {
    val draftState = remember {
        mutableStateOf(
            AppState(
                game = mutableStateOf(appState.game.value),
                page = mutableStateOf(appState.page.value),
                error = mutableStateOf(appState.error.value),
                backPage = mutableStateOf(appState.backPage.value),
                isLoading = mutableStateOf(appState.isLoading.value),
                audioPool = appState.audioPool,
                theme = mutableStateOf(appState.theme.value),
                playerName = mutableStateOf(appState.playerName.value)
            )
        )
    }
    val draftCoreConfig = remember { mutableStateOf(loadCoreConfig()) }
    var currentVol by remember {
        val masterVol = appState.audioPool.getMasterVolume()
        val isMuted = appState.audioPool.isPoolMuted()
        val min = appState.audioPool.getMasterVolumeRange()?.first

        if (isMuted) mutableStateOf(min ?: -20f)
        else mutableStateOf(masterVol ?: 0f)
    }

    ScaffoldView(
        appState = appState,
        title = "Definições",
        previousPageContent = {
            PreviousPage { setPage(appState, getCurrentState().backPage.value) }
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

                GameSection(draftState)

                CoreConfigSection(
                    coreConfig = draftCoreConfig.value,
                    onConfigChange = { draftCoreConfig.value = it }
                )

                AudioSection(
                    currentVol = currentVol,
                    onVolumeChange = { currentVol = it },
                )

                AppearanceSection(
                    draftState = draftState,
                    appTheme = appState.theme.value
                )

                ApplyButton {
                    scope.launch {
                        val newState = applySettings(appState, draftState.value, draftCoreConfig.value, currentVol)
                        // update draft state with the applied settings
                        runBlocking {
                            draftState.value = newState.copy()
                            draftCoreConfig.value = loadCoreConfig()
                            appState = newState
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReversiScope.GameSection(draftState: MutableState<AppState>) {
    SettingsSection(title = "Jogo") {
        ReversiTextField(
            value = draftState.value.playerName ?: "",
            onValueChange = { setAppState(draftState,(playerName = it) },
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
        val volumePercent = volumeDbToPercent(currentVol, minVol, maxVol)
        val volumeLabel = if (currentVol <= minVol) "Mudo" else "$volumePercent%"

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
    draftState: MutableState<AppState>,
    appTheme: AppTheme
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
                    ReversiText(draftState.value.theme.name)
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
                            setAppState(draftState,(theme = entry.appTheme)
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

private suspend fun applySettings(
    appState: AppState,
    draft: AppState,
    draftCoreConfig: CoreConfig,
    volume: Float
): AppState {
    setLoading(appState,(true)

    val current = appState
    val oldTheme = current.theme

    // try to load newStorage (can fail if config is invalid)
    val error = runStorageHealthCheck(draftCoreConfig)
    if (error == null) {
        LOGGER.info("Storage settings are valid.")
        saveCoreConfig(draftCoreConfig)
    } else {
        LOGGER.severe("Storage settings are invalid: ${error.message}")
        setError(appState,(
            error = Exception(
                "Invalid storage settings. " +
                        "Rolling Back. Please check your configuration and try again.",
                error
            ),
            errorType = ErrorType.WARNING
        )
    }

    val playingAudios = current.audioPool.getPlayingAudios()

    val loadedAudioPool = loadGameAudioPool(draft.theme) { error ->
        setError(appState,(error)
    }

    current.audioPool.merge(loadedAudioPool)

    parseVolume(volume, current)

    val currGame = current.game
    val currGameName = currGame.currGameName
    val newGame = if (currGameName != null) {
        currGame.saveEndGame()
        try {
            loadGame(currGameName, draft.playerName, currGame.myPiece).copy(config = draftCoreConfig)
        } catch (e: Exception) {
            LOGGER.severe("Failed to load game '$currGameName': ${e.message}")
            setError(appState,(
                error = Exception("Failed to load game '$currGameName': ${e.message}. "),
                errorType = ErrorType.WARNING
            )
            currGame.copy(config = draftCoreConfig)
        }
    }
    else {
        currGame.copy(config = draftCoreConfig)
    }

    setAppState(appState,(
        game = newGame.reloadConfig(),
        playerName = draft.playerName,
        theme = draft.theme,
        audioPool = current.audioPool
    )

    for (audio in playingAudios) {
        val audioToPlay = when (audio) {
            oldTheme.backgroundMusic -> draft.theme.backgroundMusic
            oldTheme.gameMusic -> draft.theme.gameMusic
            else -> null
        }
        if (audioToPlay != null && !current.audioPool.isPlaying(audioToPlay)) {
            current.audioPool.play(audioToPlay)
            LOGGER.info("Resuming audio: $audioToPlay")
        }
    }

    val loadedAudios = current.audioPool.pool.map { it.id }
    LOGGER.info("Loaded audios after applying settings: $loadedAudios")
    LOGGER.info("Core config saved: storageType=${draftCoreConfig.gameStorageType}")

    delay(100)

    setLoading(appState,(false)
    return appState
}

private fun parseVolume(volume: Float, current: AppState) {
    val minVol = current.audioPool.getMasterVolumeRange()?.first ?: -20f
    if (volume <= minVol) {
        current.audioPool.mute(true)
    } else {
        current.audioPool.mute(false)
        current.audioPool.setMasterVolume(volume)
    }
}