package pt.isel.reversi.app.state

import androidx.compose.runtime.MutableState
import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ErrorType.Companion.toReversiException
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioPool

/**
 * Updates the game state within the application state.
 *
 * @param game the new [Game] state to be set
 * @return the updated [AppState] with the new game state
 */
fun MutableState<AppState>.setGame(game: Game) {
    LOGGER.info("Set new game state")
    value = value.copy(game = game)
}

/**
 * Updates the current page in the application state.
 * Blocks page changes if there is an existing error.
 * and clears informational errors this before changing the page.
 *
 * @param page the new [Page] to be set
 * @return the updated [AppState] with the new page
 */
fun MutableState<AppState>.setPage(page: Page, backPage: Page? = null) {
    if (page == value.page) {
        LOGGER.info("Page is the same: ${page.name}, no changes made")
        return
    }
    checkAndClearInfoError()
    val error = value.error

    if (error != null) return

    if (backPage != null) {
        setBackPage(value.page)
    } else {
        autoBackPage(page)
    }
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
    theme: AppTheme = value.theme,
) {
    LOGGER.info("Set entire app state")

    if (page != value.page) {
        setPage(page, backPage)
    } else {
        if (backPage != null) {
            setBackPage(backPage)
        }
    }

    if (error !is ReversiException && error != null)
        setError(error.toReversiException(ErrorType.CRITICAL))

    value = value.copy(
        game = game,
        audioPool = audioPool,
        theme = theme,
    )
}

/**
 * Retrieves the [AudioPool] from the current [AppState].
 *
 * @return the [AudioPool] instance from the [AppState]
 */
fun MutableState<AppState>.getStateAudioPool() = value.audioPool


/**
 * Updates the error state within the application state.
 *
 * @param error the new [Exception] or [ReversiException] to be set
 * @return the updated [AppState] with the new error state
 */
fun MutableState<AppState>.setError(error: Exception?) {
    LOGGER.info("Set error: ${error?.message ?: "null"}")
    val newError = error as? ReversiException ?: error?.toReversiException(ErrorType.CRITICAL)
    value = value.copy(error = newError)
}

/**
 * Determines the back page based on the new page being set.
 *
 * @param newPage the new [Page] to be set
 * @return the determined back [Page]
 */
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