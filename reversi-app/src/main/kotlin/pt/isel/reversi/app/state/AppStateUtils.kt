package pt.isel.reversi.app.state

import androidx.compose.runtime.MutableState
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ErrorType.Companion.toReversiException
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioPool


fun MutableState<AppState>.setGame(game: Game) {
    LOGGER.info("Set new game state")
    value = value.copy(game = game)
}

fun MutableState<AppState>.setPage(page: Page) {
    if (page == value.page) {
        LOGGER.info("Page is the same: ${page.name}, no changes made")
        return
    }
    checkAndClearInfoError()
    val error = value.error

    if (error != null) return

    autoBackPage(page)
    LOGGER.info("Set page ${page.name}")
    value = value.copy(page = page, error = error)
}

private fun MutableState<AppState>.checkAndClearInfoError() {
    val error = value.error
    if (error?.type == ErrorType.INFO) {
        LOGGER.info("Clearing info error")
        value = value.copy(error = null)
    }
}

fun MutableState<AppState>.setAppState(
    game: Game = value.game,
    page: Page = value.page,
    error: Exception? = null,
    backPage: Page? = null,
    audioPool: AudioPool = value.audioPool,
) {
    LOGGER.info("Set entire app state")

    if (page != value.page) {
        setPage(page)
    } else {
        value
    }

    if (error !is ReversiException )
        setError(error?.toReversiException(ErrorType.CRITICAL))

    if (backPage != null) {
        setBackPage(backPage)
    }

    value = value.copy(
        game = game,
        audioPool = audioPool
    )
}

/**
 * Retrieves the [AudioPool] from the current [AppState].
 *
 * @return the [AudioPool] instance from the [AppState]
 */
fun MutableState<AppState>.getStateAudioPool() = value.audioPool

fun MutableState<AppState>.setError(error: Exception?) {
    LOGGER.info("Set error: ${error?.message ?: "null"}")
    val newError = error as? ReversiException ?: error?.toReversiException(ErrorType.CRITICAL)
    value = value.copy(error = newError)
}

private fun MutableState<AppState>.autoBackPage(newPage: Page) {
    val page = value.page
    val backPage = when (newPage) {
        Page.LOBBY -> Page.MAIN_MENU
        Page.GAME -> Page.MAIN_MENU
        else -> page
    }
    LOGGER.info("Set back page: ${backPage.name}")
    value = value.copy(backPage = backPage)
}

fun MutableState<AppState>.setBackPage(backPage: Page) {
    LOGGER.info("Set back page: ${backPage.name}")
    value = value.copy(backPage = backPage)
}

fun MutableState<AppState>.setLoading(isLoading: Boolean) {
    if (isLoading == value.isLoading) return
    LOGGER.info("Set loading: $isLoading")
    value = value.copy(isLoading = isLoading)
}