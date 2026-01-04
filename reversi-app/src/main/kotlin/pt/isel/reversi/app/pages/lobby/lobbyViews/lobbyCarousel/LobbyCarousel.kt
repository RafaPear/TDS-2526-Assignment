package pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel

import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.LobbyCarousel(
    currentGameName: String?,
    games: List<Game>,
    viewModel: LobbyViewModel,
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
        val gameName = game.currGameName
        LOGGER.info("lobbyCarousel: Refresh iniciado para $gameName")

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
                delay(delayMillis)
            }
        } finally {
            LOGGER.info("lobbyCarousel: Refresh terminado para $gameName")
        }
    }

    Row {
        Search(searchQuery) { query ->
            scope.launch { pagerState.scrollToPage(0) }
            searchQuery = query
        }

        buttonRefresh()
    }

    Spacer(Modifier.weight(1f))

    BoxWithConstraints {
        LobbyCarouselView(
            currentGameName = currentGameName,
            pagerState = pagerState,
            games = gamesToShow,
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

    Spacer(Modifier.weight(1f))

    PageIndicators(gamesToShow.size, pagerState.currentPage)
}