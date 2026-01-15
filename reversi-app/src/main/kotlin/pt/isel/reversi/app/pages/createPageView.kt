package pt.isel.reversi.app.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.Snapshot
import pt.isel.reversi.app.pages.aboutPage.AboutPage
import pt.isel.reversi.app.pages.aboutPage.AboutPageViewModel
import pt.isel.reversi.app.pages.game.GamePage
import pt.isel.reversi.app.pages.game.GamePageViewModel
import pt.isel.reversi.app.pages.lobby.LobbyMenu
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.menu.MainMenu
import pt.isel.reversi.app.pages.menu.MainMenuViewModel
import pt.isel.reversi.app.pages.newGamePage.NewGamePage
import pt.isel.reversi.app.pages.newGamePage.NewGameViewModel
import pt.isel.reversi.app.pages.settingsPage.SettingsPage
import pt.isel.reversi.app.pages.settingsPage.SettingsViewModel
import pt.isel.reversi.app.pages.statisticsPage.StatisticsPage
import pt.isel.reversi.app.pages.statisticsPage.StatisticsPageViewModel
import pt.isel.reversi.app.pages.winnerPage.WinnerPage
import pt.isel.reversi.app.pages.winnerPage.WinnerPageViewModel
import pt.isel.reversi.app.state.ReversiScope
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.app.state.setPage
import pt.isel.reversi.core.Game

@Composable
fun Page.createPageView(
    vm: ViewModel<out UiState>,
    game: MutableState<Game>,
    playerName: MutableState<String?>,
    pagesState: MutableState<PagesState>,
): @Composable ReversiScope.() -> Unit =
    when (this@createPageView) {
        Page.MAIN_MENU -> if (vm is MainMenuViewModel) {
            { MainMenu(viewModel = vm, onLeave = {}) }
        } else {
            {}
        }

        Page.GAME -> if (vm is GamePageViewModel) {
            {
                GamePage(viewModel = vm) {
                    Snapshot.withMutableSnapshot {
                        game.setGame(it)
                        pagesState.setPage(Page.MAIN_MENU)
                    }
                }
            }
        } else {
            {}
        }

        Page.SETTINGS -> if (vm is SettingsViewModel) {
            { SettingsPage(viewModel = vm) { pagesState.setPage(pagesState.value.backPage) } }
        } else {
            {}
        }

        Page.ABOUT -> if (vm is AboutPageViewModel) {
            { AboutPage(viewModel = vm) { pagesState.setPage(pagesState.value.backPage) } }
        } else {
            {}
        }

        Page.NEW_GAME -> if (vm is NewGameViewModel) {
            {
                NewGamePage(
                    viewModel = vm,
                    playerNameChange = { name: String -> playerName.value = name }
                ) { pagesState.setPage(Page.MAIN_MENU) }
            }
        } else {
            {}
        }

        Page.LOBBY -> if (vm is LobbyViewModel) {
            { LobbyMenu(vm) { pagesState.setPage(Page.MAIN_MENU) } }
        } else {
            {}
        }

        Page.STATISTICS -> if (vm is StatisticsPageViewModel) {
            { StatisticsPage(viewModel = vm) { pagesState.setPage(Page.MAIN_MENU) } }
        } else {
            {}
        }

        Page.WINNER -> if (vm is WinnerPageViewModel) {
            { WinnerPage(viewModel = vm) { pagesState.setPage(Page.MAIN_MENU) } }
        } else {
            {}
        }

        Page.NONE -> {
            {}
        }
    }
