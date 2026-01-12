package pt.isel.reversi.app.pages.settingsPage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.gameAudio.loadGameAudioPool
import pt.isel.reversi.app.runStorageHealthCheck
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.CoreConfig
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.loadCoreConfig
import pt.isel.reversi.core.saveCoreConfig
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioPool

data class SettingsUiState(
    override val screenState: ScreenState = ScreenState()
) : UiState() {
    override fun updateScreenState(newScreenState: ScreenState) =
        copy(screenState = newScreenState)
}

class SettingsViewModel(
    private val scope: CoroutineScope,
    private val setTheme: (AppTheme) -> Unit,
    private val setPlayerName: (String?) -> Unit,
    private val audioPool: AudioPool,
    globalError: ReversiException? = null
): ViewModel {
    private val _uiState = mutableStateOf(
        SettingsUiState(
            screenState = ScreenState(error = globalError)
        )
    )

    override val uiState: State<SettingsUiState> = _uiState

    override fun setError(error: Exception?) =
        _uiState.setError(error)

    suspend fun applySettings(
        oldTheme: AppTheme,
        newName: String?,
        newTheme: AppTheme,
        draftCoreConfig: CoreConfig,
        volume: Float
    ) {
        _uiState.setLoading(true)

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

            val playingAudios = audioPool.getPlayingAudios()

            val loadedAudioPool = loadGameAudioPool(newTheme) { err ->
                _uiState.setError(err, ErrorType.WARNING)
            }

            audioPool.merge(loadedAudioPool)

            // Apply audio volume
            parseVolume(volume, audioPool)

            // Apply theme and player name
            Snapshot.withMutableSnapshot {
                setPlayerName(newName)
                setTheme(newTheme)
            }

            // Resume previously playing theme-related audios
            for (audio in playingAudios) {
                val audioToPlay = when (audio) {
                    oldTheme.backgroundMusic -> newTheme.backgroundMusic
                    oldTheme.gameMusic -> newTheme.gameMusic
                    else -> null
                }
                if (audioToPlay != null && !audioPool.isPlaying(audioToPlay)) {
                    audioPool.play(audioToPlay)
                    LOGGER.info("Resuming audio: $audioToPlay")
                }
            }

            val loadedAudios = audioPool.pool.map { it.id }
            LOGGER.info("Loaded audios after applying settings: $loadedAudios")
            // Small delay for UX
            delay(100)
        } catch (e: Exception) {
            LOGGER.severe("Failed to apply settings: ${e.message}")
        } finally {
            _uiState.setLoading(false)
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
