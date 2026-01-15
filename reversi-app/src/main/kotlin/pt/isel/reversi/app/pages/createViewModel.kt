package pt.isel.reversi.app.pages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.pages.aboutPage.AboutPageViewModel
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.menu.MainMenuViewModel
import pt.isel.reversi.app.pages.newGamePage.NewGameViewModel
import pt.isel.reversi.app.pages.settingsPage.SettingsViewModel
import pt.isel.reversi.app.pages.statisticsPage.StatisticsPageViewModel
import pt.isel.reversi.app.pages.winnerPage.WinnerPageViewModel
import pt.isel.reversi.app.state.AppStateImpl
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.app.state.setGlobalError
import pt.isel.reversi.app.state.setPage
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.GameService
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.audio.AudioPool


fun Page.createViewModel(
    scope: CoroutineScope,
    appState: AppStateImpl,
    game: MutableState<Game>,
    audioPool: MutableState<AudioPool>,
    themeState: MutableState<AppTheme>,
    globalError: MutableState<ReversiException?>,
    playerName: MutableState<String?>,
    pagesState: MutableState<PagesState>,
) = when (this) {
    Page.MAIN_MENU -> MainMenuViewModel(
        appState,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
        setPage = { pagesState.setPage(it) }
    )

    Page.GAME -> GamePageViewModel(
        game.value,
        globalError = globalError.value,
        scope = scope,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
        audioPlayMove = {
            audioPool.value.run {
                stop(themeState.value.placePieceSound)
                play(themeState.value.placePieceSound)
            }
        },
        setPage = { pagesState.setPage(it , backPage = Page.MAIN_MENU) },
        setGame = { game.setGame(it) },
    )

    Page.SETTINGS -> SettingsViewModel(
        scope,
        appState,
        setTheme = { themeState.value = it },
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
        setPlayerName = {
            Snapshot.withMutableSnapshot {
                playerName.value = it
                val newName = it ?: return@withMutableSnapshot
                val gameState = game.value.gameState ?: return@withMutableSnapshot
                val myPiece = game.value.myPiece ?: return@withMutableSnapshot
                game.setGame(
                    game.value.copy(
                        gameState = gameState.changeName(newName, myPiece)
                    )
                )
            }
        },
        saveGame = { game.value.saveEndGame() ; game.setGame(Game(service = GameService())) },
        globalError = globalError.value
    )

    Page.ABOUT -> AboutPageViewModel(
        globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
    )

    Page.NEW_GAME -> NewGameViewModel(
        scope = scope,
        appState = appState,
        playerName = playerName.value,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
        createGame = { newGame ->
            Snapshot.withMutableSnapshot {
                game.setGame(newGame)
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
                game.setGame(it)
                pagesState.setPage(Page.GAME, backPage = Page.MAIN_MENU)
            }
        },
        globalError = globalError.value,
    )

    Page.STATISTICS -> StatisticsPageViewModel(
        scope = scope,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) },
    )

    Page.WINNER -> WinnerPageViewModel(
        game.value,
        globalError = globalError.value,
        setGlobalError = { it, type -> globalError.setGlobalError(it, type) }
    )

    Page.NONE -> null
}