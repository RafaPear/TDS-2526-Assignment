package pt.isel.reversi.app.gameAudio

import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioModifier
import pt.isel.reversi.utils.audio.AudioPool
import pt.isel.reversi.utils.audio.AudioPool.Companion.buildAudioPool
import pt.isel.reversi.utils.audio.AudioWrapper.Companion.loadAudio
import pt.isel.reversi.utils.loadResource

/**
 * Loads the game's audio pool from the resources.
 * @return An AudioPool containing all loaded audio tracks.
 */
fun loadGameAudioPool(theme: AppTheme, mainFolder: String = "audios/"): AudioPool {
    val audioNames = listOf(
        theme.backgroundMusic,
        theme.gameMusic,
        theme.placePieceSound
    )
    val audioPaths = try {
        loadResource(mainFolder).listFiles()?.mapNotNull {
            val name = it.name.substringBeforeLast('.')
            try {
                when (name) {
                    in setOf(theme.backgroundMusic, theme.gameMusic) -> loadAudio(
                        name,
                        it.toURI().toURL(),
                        AudioModifier().setToLoopInfinitely()
                    )
                    in audioNames -> loadAudio(name, it.toURI().toURL())
                    else -> null
                }
            } catch (e: Exception) {
                LOGGER.warning("Failed to load audio $name: ${e.message}")
                null
            }
        } ?: emptyList()
    } catch (e: Exception) {
        LOGGER.warning("Could not load audio resources from $mainFolder: ${e.message}")
        emptyList()
    }
    return buildAudioPool { for (audio in audioPaths) add(audio) }
}