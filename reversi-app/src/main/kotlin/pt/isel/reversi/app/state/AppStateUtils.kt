@file:Suppress("UNCHECKED_CAST")

package pt.isel.reversi.app.state

import androidx.compose.runtime.MutableState
import pt.isel.reversi.app.pages.Page
import pt.isel.reversi.app.pages.PagesState
import pt.isel.reversi.app.pages.UiState
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.exceptions.ErrorType.Companion.toReversiException
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.LOGGER

/**
 * Updates the current page in the application state.
 * @param page The new page to set.
 * @param backPage The new back page (auto-calculated if null).
 */
fun MutableState<PagesState>.setPage(page: Page, backPage: Page? = null) {
    if (page == value.page) {
        LOGGER.info("Page is the same: ${page.name}, no changes made")
        return
    }

    val newBackPage =
        if (page == Page.MAIN_MENU) {
            Page.NONE
        } else {
            backPage ?: value.page
        }

    LOGGER.info("Set page ${page.name}, backPage: ${newBackPage.name}")
    value = value.copy(
        page = page,
        backPage = newBackPage
    )
}

/**
 * Sets the game state.
 * @param game The new game state.
 */
fun MutableState<Game>.setGame(game: Game) {
    LOGGER.info("Set new game state")
    value = game
}

/**
 * Retrieves the AudioPool from the current AppState.
 * @param appState The application state holder.
 * @return the AudioPool instance.
 */
fun getStateAudioPool(appState: AppState) = appState.audioPool

/**
 * Updates the loading state for any UiState implementation.
 *
 * @param T The concrete UiState type.
 * @param isLoading Whether the app is loading.
 */
fun <T : UiState> MutableState<T>.setLoading(isLoading: Boolean) {
    if (isLoading == value.screenState.isLoading) return
    LOGGER.info("Set loading: $isLoading")
    val newScreenState = value.screenState.copy(isLoading = isLoading)
    value = value.updateScreenState(newScreenState) as T
}

/**
 * Updates the error state for any UiState implementation.
 *
 * @param T The concrete UiState type.
 * @param error The new error.
 */
fun <T : UiState> MutableState<T>.setError(error: Exception?, type: ErrorType = ErrorType.CRITICAL) {
    LOGGER.info("Set error: ${error?.message ?: "null"}")
    val newError = error as? ReversiException ?: error?.toReversiException(type)
    val newScreenState = value.screenState.copy(error = newError)
    value = value.updateScreenState(newScreenState) as T
}

fun MutableState<ReversiException?>.setGlobalError(error: Exception?, type: ErrorType = ErrorType.CRITICAL) {
    LOGGER.info("Set global error: ${error?.message ?: "null"}")
    val newError = error as? ReversiException ?: error?.toReversiException(type)
    value = newError
}
