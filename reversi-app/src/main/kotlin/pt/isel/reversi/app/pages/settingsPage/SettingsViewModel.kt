package pt.isel.reversi.app.pages.settingsPage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.gameAudio.loadGameAudioPool
import pt.isel.reversi.app.state.*
import pt.isel.reversi.app.utils.runStorageHealthCheck
import pt.isel.reversi.core.CoreConfig
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.loadCoreConfig
import pt.isel.reversi.core.saveCoreConfig
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.TRACKER
import pt.isel.reversi.utils.audio.AudioPool

data class SettingsUiState(
    override val screenState: ScreenState = ScreenState(),
    val draftPlayerName: String?,
    val draftTheme: AppTheme,
    val draftCoreConfig: CoreConfig,
    val currentVol: Float
) : UiState() {
    override fun updateScreenState(newScreenState: ScreenState) =
        copy(screenState = newScreenState)
}

class SettingsViewModel(
    private val scope: CoroutineScope,
    private val appState: AppState,
    private val globalError: ReversiException? = null,
    private val setTheme: (AppTheme) -> Unit,
    private val setPlayerName: (String?) -> Unit,
    private val setGlobalError: (Exception?) -> Unit,
) : ViewModel {

    private val _uiState = mutableStateOf(
        SettingsUiState(
            draftPlayerName = appState.playerName,
            draftTheme = appState.theme,
            draftCoreConfig = loadCoreConfig(),
            currentVol = 0f,
            screenState = ScreenState(error = globalError)
        )
    )

    init {
        val masterVol = appState.audioPool.getMasterVolume()
        val isMuted = appState.audioPool.isPoolMuted()
        val min = appState.audioPool.getMasterVolumeRange()?.first

        val currentVol = if (isMuted) min ?: -20f
        else masterVol ?: 0f

        _uiState.value = _uiState.value.copy(
            currentVol = currentVol,
        )
    }

    override val uiState: State<SettingsUiState> = _uiState

    override fun setError(error: Exception?) =
        if (globalError != null) {
            setGlobalError(error)
        } else
            _uiState.setError(error)

    fun setDraftPlayerName(name: String?) {
        _uiState.value = _uiState.value.copy(draftPlayerName = name)
    }

    fun setDraftTheme(theme: AppTheme) {
        _uiState.value = _uiState.value.copy(draftTheme = theme)
    }

    fun setDraftCoreConfig(coreConfig: CoreConfig) {
        _uiState.value = _uiState.value.copy(draftCoreConfig = coreConfig)
    }

    fun setCurrentVol(volume: Float) {
        _uiState.value = _uiState.value.copy(currentVol = volume)
    }

    fun applySettings(
        oldTheme: AppTheme,
        newName: String?,
        newTheme: AppTheme,
        draftCoreConfig: CoreConfig,
        volume: Float
    ) {
        _uiState.setLoading(true)
        TRACKER.trackFunctionCall(details = "Apply settings clicked")

        scope.launch {
            try {
                // check if storage type changed and test connection if needed
                val currentCoreConfig = loadCoreConfig()
                if (currentCoreConfig != draftCoreConfig) {
                    LOGGER.info("Storage type changed from ${currentCoreConfig.gameStorageType} to ${draftCoreConfig.gameStorageType}, testing connectivity...")
                    val exception = runStorageHealthCheck(testConf = draftCoreConfig, save = true)
                    if (exception != null) {
                        _uiState.setError(exception, ErrorType.WARNING)
                        LOGGER.severe("Storage type change failed: ${exception.message}")
                    } else {
                        saveCoreConfig(draftCoreConfig)
                        LOGGER.info("Core config saved: storageType=${draftCoreConfig.gameStorageType}")
                    }
                }

                val playingAudios = appState.audioPool.getPlayingAudios()

                val loadedAudioPool = loadGameAudioPool(newTheme) { err ->
                    _uiState.setError(err, ErrorType.WARNING)
                }

                appState.audioPool.merge(loadedAudioPool)

                // Apply audio volume
                parseVolume(volume, appState.audioPool)

                // Apply theme and player name
                Snapshot.withMutableSnapshot {
                    setPlayerName(newName?.ifEmpty { appState.playerName })
                    setTheme(newTheme)
                }

                // Resume previously playing theme-related audios
                for (audio in playingAudios) {
                    val audioToPlay = when (audio) {
                        oldTheme.backgroundMusic -> newTheme.backgroundMusic
                        oldTheme.gameMusic -> newTheme.gameMusic
                        else -> null
                    }
                    if (audioToPlay != null && !appState.audioPool.isPlaying(audioToPlay)) {
                        appState.audioPool.play(audioToPlay)
                        LOGGER.info("Resuming audio: $audioToPlay")
                    }
                }

                val loadedAudios = appState.audioPool.pool.map { it.id }
                LOGGER.info("Loaded audios after applying settings: $loadedAudios")
                // Small delay for UX
                delay(100)
            } catch (e: Exception) {
                LOGGER.severe("Failed to apply settings: ${e.message}")
            } finally {
                _uiState.setLoading(false)
            }
        }
    }


    private fun parseVolume(volume: Float, current: AudioPool) {
        val minVol = current.getMasterVolumeRange()?.first ?: -20f
        if (volume <= minVol) {
            current.mute(true)
        } else {
            current.mute(false)
            current.setMasterVolume(volume)
        }
    }
}
