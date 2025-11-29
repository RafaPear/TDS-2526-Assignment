package pt.isel.reversi.app.gameAudio

import pt.isel.reversi.app.BACKGROUND_MUSIC
import pt.isel.reversi.app.MEGALOVANIA
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
fun loadGameAudioPool(mainFolder: String = "audios/"): AudioPool {
    val audioPaths = try {
        loadResource(mainFolder).listFiles()?.mapNotNull {
            val name = it.name.substringBeforeLast('.')
            try {
                if (name in setOf(BACKGROUND_MUSIC, MEGALOVANIA))
                    loadAudio(
                        name,
                        it.toURI().toURL(),
                        AudioModifier().setToLoopInfinitely()
                    )
                else loadAudio(name, it.toURI().toURL())
            } catch (e: Exception) {
                LOGGER.warning("Failed to load audio $name: ${e.message}")
                null
            }
        } ?: emptyList()
    } catch (e: IllegalArgumentException) {
        LOGGER.warning("Audio resource directory missing: ${e.message}")
        emptyList()
    }
    return buildAudioPool { for (audio in audioPaths) add(audio) }
}