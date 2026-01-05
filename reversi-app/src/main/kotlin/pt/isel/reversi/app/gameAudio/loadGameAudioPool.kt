package pt.isel.reversi.app.gameAudio

import pt.isel.reversi.app.BACKGROUND_MUSIC
import pt.isel.reversi.app.MEGALOVANIA
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioModifier
import pt.isel.reversi.utils.audio.AudioPool
import pt.isel.reversi.utils.audio.AudioPool.Companion.buildAudioPool
import pt.isel.reversi.utils.audio.AudioWrapper.Companion.loadAudio
import pt.isel.reversi.utils.loadResourcesFromFolder

/**
 * Loads the game's audio pool from the resources.
 * @return An AudioPool containing all loaded audio tracks.
 */
fun loadGameAudioPool(mainFolder: String = "audios/"): AudioPool {
    val audioPaths = loadResourcesFromFolder(mainFolder) { fileName, url ->
        val name = fileName.substringBeforeLast('.')
        try {
            if (name in setOf(BACKGROUND_MUSIC, MEGALOVANIA))
                loadAudio(name, url, AudioModifier().setToLoopInfinitely())
            else
                loadAudio(name, url)
        } catch (e: Exception) {
            LOGGER.warning("Failed to load audio $name: ${e.message}")
            null
        }
    }

    return buildAudioPool { for (audio in audioPaths) add(audio) }
}