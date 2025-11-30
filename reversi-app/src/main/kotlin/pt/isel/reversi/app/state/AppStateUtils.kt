package pt.isel.reversi.app.state

import androidx.compose.runtime.MutableState
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.audio.AudioPool

/**
 * Updates the game state within the application state.
 *
 * @param appState the mutable state holding the current [AppState]
 * @param game the new [Game] state to be set
 * @return the updated [AppState] with the new game state
 */
fun setGame(appState: MutableState<AppState>, game: Game): AppState {
    LOGGER.info("Set new game state")
    return appState.value.copy(game = game)
}


/**
 * Updates the current page in the application state.
 * Blocks page changes if there is an existing error.
 * and clears informational errors this before changing the page.
 *
 * @param appState the mutable state holding the current [AppState]
 * @param page the new [Page] to be set
 * @return the updated [AppState] with the new page
 */
fun setPage(appState: MutableState<AppState>, page: Page): AppState {
    val newAppState = checkAndClearInfoError(appState)

    val error = newAppState.error

    if (error != null) return newAppState

    LOGGER.info("Set page ${page.name}")
    val backPage = setBackPage(appState, newPage = page)
    return newAppState.copy(page = page, backPage = backPage)
}

/**
 * Checks if the current error in the application state is of type INFO.
 * If so, it clears the error from the application state.
 *
 * @param appState the mutable state holding the current [AppState]
 */
private fun checkAndClearInfoError(appState: MutableState<AppState>): AppState {
    val error = appState.value.error
    return if (error?.type == ErrorType.INFO) {
        LOGGER.info("Clearing info error")
        appState.value.copy(error = null)
    } else
        appState.value
}

/**
 * Sets the entire application state with the provided parameters.
 * If the page is changing, it checks and clears any informational errors.
 *
 * @param appState the mutable state holding the current [AppState]
 * @param game the new [Game] state to be set (default is the current game)
 * @param page the new [Page] to be set (default is the current page)
 * @param error the new [ReversiException] to be set (default is the current error)
 * @param audioPool the new [AudioPool] to be set (default is the current audio pool)
 * @return the updated [AppState] with the new values
 */
fun setAppState(
    appState: MutableState<AppState>,
    game: Game = appState.value.game,
    page: Page = appState.value.page,
    error: ReversiException? = null,
    audioPool: AudioPool = appState.value.audioPool,
): AppState {
    LOGGER.info("Set entire app state")
    val newAppState =
        if (page != appState.value.page) {
            setPage(appState, page)
        } else {
            appState.value
        }
    return newAppState.copy(
        game = game,
        error = error ?: newAppState.error,
        audioPool = audioPool
    )
}

/**
 * Retrieves the [AudioPool] from the current [AppState].
 *
 * @param appState the mutable state holding the current [AppState]
 * @return the [AudioPool] instance from the [AppState]
 */
fun getStateAudioPool(appState: MutableState<AppState>) = appState.value.audioPool

/**
 * Updates the error state within the application state.
 *
 * @param appState the mutable state holding the current [AppState]
 * @param error the new [ReversiException] to be set
 * @return the updated [AppState] with the new error state
 */
fun setError(appState: MutableState<AppState>, error: ReversiException?): AppState {
    LOGGER.info("Set error: ${error?.message ?: "null"}")
    return appState.value.copy(error = error)
}

/**
 * Determines the back page based on the new page being set.
 *
 * @param appState the mutable state holding the current [AppState]
 * @param newPage the new [Page] to be set
 * @return the determined back [Page]
 */
private fun setBackPage(appState: MutableState<AppState>, newPage: Page): Page {
    val page = appState.value.page
    LOGGER.info("Set back page: ${page.name}")
    return when (newPage) {
        Page.GAME -> Page.MAIN_MENU
        else -> page
    }
}