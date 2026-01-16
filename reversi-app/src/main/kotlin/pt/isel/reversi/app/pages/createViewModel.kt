package pt.isel.reversi.app.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import pt.isel.reversi.app.pages.aboutPage.AboutPageViewModel
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.menu.MainMenuViewModel
import pt.isel.reversi.app.pages.newGamePage.NewGameViewModel
import pt.isel.reversi.app.pages.settingsPage.SettingsViewModel
import pt.isel.reversi.app.pages.winnerPage.WinnerPageViewModel
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.exceptions.ReversiException


fun Page.createViewModel(
    scope: CoroutineScope,
    appState: AppStateImpl,
    gameSession: MutableState<GameSession>,
    audioThemeState: MutableState<AudioThemeState>,
    globalError: MutableState<ReversiException?>,
    pagesState: MutableState<PagesState>,
) = when (this) {
    Page.MAIN_MENU -> MainMenuViewModel(
        appState,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
        setPage = { pagesState.setPage(it) }
    )

    Page.GAME -> GamePageViewModel(
        gameSession.value.game,
        globalError = globalError.value,
        scope = scope,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
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
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
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
        globalError = globalError.value
    )

    Page.ABOUT -> AboutPageViewModel(
        globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
    )

    Page.NEW_GAME -> NewGameViewModel(
        scope = scope,
        appState = appState,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
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
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
        pickGame = {
            Snapshot.withMutableSnapshot {
                gameSession.setGame(it)
                pagesState.setPage(Page.GAME, backPage = Page.MAIN_MENU)
            }
        },
        globalError = globalError.value,
    )

    Page.WINNER -> WinnerPageViewModel(
        gameSession.value.game,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) }
    )

    Page.NONE -> null
}