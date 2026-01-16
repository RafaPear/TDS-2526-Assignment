package pt.isel.reversi.app.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import pt.isel.reversi.app.app.state.*
import pt.isel.reversi.app.pages.aboutPage.AboutPageViewModel
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.menu.MainMenuViewModel
import pt.isel.reversi.app.pages.newGamePage.NewGameViewModel
import pt.isel.reversi.app.pages.settingsPage.SettingsViewModel
import pt.isel.reversi.app.pages.winnerPage.WinnerPageViewModel


fun Page.createViewModel(
    scope: CoroutineScope,
    appState: AppStateImpl,
    gameSession: MutableState<GameSession>,
    audioThemeState: MutableState<AudioThemeState>,
    pagesState: MutableState<PagesState>,
) = when (this) {
    Page.MAIN_MENU -> MainMenuViewModel(
        appState,
        globalError = pagesState.value.globalError,
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) },
        setPage = { pagesState.setPage(it) }
    )

    Page.GAME -> GamePageViewModel(
        gameSession.value.game,
        globalError = pagesState.value.globalError,
        scope = scope,
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) },
        audioPlayMove = {
            audioThemeState.value.audioPool.run {
                stop(audioThemeState.value.theme.placePieceSound)
                play(audioThemeState.value.theme.placePieceSound)
            }
        },
        setPage = { pagesState.setPage(it, backPage = Page.MAIN_MENU) },
        setGame = { gameSession.setGame(it) },
    )

    Page.SETTINGS -> SettingsViewModel(
        scope,
        appState,
        setTheme = { audioThemeState.setTheme(it)},
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) },
        setPlayerName = {
            gameSession.setPlayerName(it)
            val newName = it ?: return@SettingsViewModel
            val gameState = gameSession.value.game.gameState ?: return@SettingsViewModel
            val myPiece = gameSession.value.game.myPiece ?: return@SettingsViewModel
            val newGameState = gameState.changeName(newName, myPiece)
            gameSession.setGame(
                gameSession.value.game.copy(
                    gameState = newGameState
                )
            )
            gameSession.value.game.saveOnlyBoard(newGameState)
        },
        saveGame = { gameSession.value.game.saveEndGame() },
        setGame = { gameSession.setGame(it) },
        globalError = pagesState.value.globalError
    )

    Page.ABOUT -> AboutPageViewModel(
        pagesState.value.globalError,
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) },
    )

    Page.NEW_GAME -> NewGameViewModel(
        scope = scope,
        appState = appState,
        globalError = pagesState.value.globalError,
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) },
        createGame = { newGame ->
            Snapshot.withMutableSnapshot {
                gameSession.setGame(newGame)
                pagesState.setPage(Page.GAME, backPage = Page.MAIN_MENU)
            }
        }
    )

    Page.LOBBY -> LobbyViewModel(
        scope = scope,
        appState = appState,
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) },
        pickGame = {
            Snapshot.withMutableSnapshot {
                gameSession.setGame(it)
                pagesState.setPage(Page.GAME, backPage = Page.MAIN_MENU)
            }
        },
        globalError = pagesState.value.globalError,
    )

    Page.WINNER -> WinnerPageViewModel(
        gameSession.value.game,
        globalError = pagesState.value.globalError,
        setGlobalError = { it, type -> pagesState.setGlobalError(it, type) }
    )

    Page.NONE -> null
}