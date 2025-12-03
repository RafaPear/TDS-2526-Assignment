package pt.isel.reversi.app.state

import androidx.compose.runtime.MutableState
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioPool

fun MutableState<AppState>.setGame(game: Game) {
    LOGGER.info("Set new game state")
    value = value.copy(game = game)
}

fun MutableState<AppState>.setPage(page: Page) {
    checkAndClearInfoError()
    val error = value.error

    if (error != null) {
        value = value.copy(error = error)
        return
    }

    LOGGER.info("Set page ${page.name}")
    setBackPage()
    value = value.copy(page = page, error = error)
}

private fun MutableState<AppState>.checkAndClearInfoError() {
    val error = value.error
    if (error != null && error.type == ErrorType.INFO) {
        LOGGER.info("Clearing info error")
        value = value.copy(error = null)
    }
}

fun MutableState<AppState>.setAppState(
    game: Game = value.game,
    page: Page = value.page,
    error: ReversiException? = value.error,
    audioPool: AudioPool = value.audioPool,
) {
    LOGGER.info("Set entire app state")
    setBackPage()
    value = AppState(
        game = game,
        page = page,
        error = error,
        audioPool = audioPool
    )
}

/**
 * Retrieves the [AudioPool] from the current [AppState].
 *
 * @return the [AudioPool] instance from the [AppState]
 */
fun MutableState<AppState>.getStateAudioPool() = value.audioPool

fun MutableState<AppState>.setError(error: ReversiException?) {
    LOGGER.info("Set error: ${error?.message ?: "null"}")
    value = value.copy(error = error)
}

private fun MutableState<AppState>.setBackPage() {
    val page = value.page
    LOGGER.info("Set back page: ${page.name}")
    value = value.copy(backPage = page)
}