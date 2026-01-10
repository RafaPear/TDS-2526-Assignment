package pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel

import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.pages.lobby.LobbyViewModel
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.utils.PageIndicators
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.utils.Search
import pt.isel.reversi.core.Game
import pt.isel.reversi.utils.LOGGER

private suspend fun PagerState.animateScroll(page: Int) {
    animateScrollToPage(
        page = page,
        animationSpec = spring(
            stiffness = 400f,
            dampingRatio = 0.75f
        )
    )
}

/**
 * Carousel component for browsing saved multiplayer games.
 * Supports search filtering, pagination, and game status indication.
 *
 * @param currentGameName Name of the currently active game for highlighting.
 * @param games List of all available games to display.
 * @param viewModel The lobby view model managing game state.
 * @param reversiScope The ReversiScope receiver for theming and utilities.
 * @param buttonRefresh Optional composable for rendering a refresh button.
 * @param onGameClick Callback invoked when a game card is selected.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.LobbyCarousel(
    currentGameName: String?,
    games: List<Game>,
    viewModel: LobbyViewModel,
    reversiScope: ReversiScope,
    buttonRefresh: @Composable () -> Unit = {},
    onGameClick: (Game) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val gamesToShow: List<Game> = remember(games, searchQuery) {
        if (searchQuery.isEmpty())
            games
        else {
            val foundGames = games.filter { it.currGameName?.contains(searchQuery, ignoreCase = true) == true }
            LOGGER.info("Search query: '$searchQuery' - Found : ${foundGames.size}")
            foundGames
        }
    }

    val pagerState = rememberPagerState(pageCount = { gamesToShow.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage, gamesToShow) {
        if (gamesToShow.isEmpty()) return@LaunchedEffect
        val game = gamesToShow.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        val leftGame = gamesToShow.getOrNull(pagerState.currentPage - 1)
        val rightGame = gamesToShow.getOrNull(pagerState.currentPage + 1)

        val gameName = game.currGameName
        val leftGameName = leftGame?.currGameName
        val rightGameName = rightGame?.currGameName
        LOGGER.info("lobbyCarousel: Refresh iniciado para $gameName, $leftGameName, $rightGameName")

        val delayMillis = when (getCardStatus(game, currentGameName)) {
            CardStatus.EMPTY,
            CardStatus.CURRENT_GAME -> 100L
            CardStatus.WAITING_FOR_PLAYERS -> 500L
            CardStatus.FULL -> 15_000L
            CardStatus.CORRUPTED -> 20_000L
        }

        try {
            while (isActive) {
                viewModel.refreshGame(game)
                if (leftGame != null)  viewModel.refreshGame(leftGame)
                if (rightGame != null) viewModel.refreshGame(rightGame)
                delay(delayMillis)
            }
        } finally {
            LOGGER.info("lobbyCarousel: Refresh terminado para $gameName, $leftGameName, $rightGameName")
        }
    }

    with(reversiScope) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Search(searchQuery) { query ->
                scope.launch { pagerState.scrollToPage(0) }
                searchQuery = query
            }

            buttonRefresh()
        }

        Spacer(Modifier.weight(1f))
    }

    // Move BoxWithConstraints outside the reversiScope
    BoxWithConstraints {
        LobbyCarouselView(
            currentGameName = currentGameName,
            pagerState = pagerState,
            games = gamesToShow,
            reversiScope = reversiScope,
            onNavButtonClick = { page ->
                scope.launch {
                    pagerState.animateScroll(page)
                }
            },
        ) { game, page ->
            scope.launch {
                if (page != pagerState.currentPage)
                    pagerState.animateScroll(page)
                delay(150L)
                onGameClick(game)
            }
        }
    }

    with(reversiScope) {
        Spacer(Modifier.weight(1f))
        PageIndicators(gamesToShow.size, pagerState.currentPage)
    }
}