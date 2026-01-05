package pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.ReversiScope
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.drawCard.GameCard
import pt.isel.reversi.core.Game
import kotlin.math.absoluteValue

@Composable
fun BoxWithConstraintsScope.LobbyCarouselView(
    currentGameName: String?,
    pagerState: PagerState,
    games: List<Game>,
    reversiScope: ReversiScope,
    onGameClick: (Game, Int) -> Unit
) {
    val availableWidth = this.maxWidth
    val availableHeight = this.maxHeight

    val maxCardWidth = (availableWidth * 0.7f).coerceAtMost(450.dp)
    val maxCardHeight = (availableHeight * 0.8f).coerceAtMost(950.dp)

    val horizontalPadding = (availableWidth / 2 - maxCardWidth / 2)

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        pageSpacing = 16.dp
    ) { page ->
        val pageOffset =
            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val distance = pageOffset.absoluteValue.coerceIn(0f, 1f)

        val scale = 0.95f + (1f - distance) * 0.12f
        val alpha = 0.2f + (1f - distance) * 0.8f
        val translation = 8.dp * distance

        val game = games[page]
        val cardState = getCardStatus(game, currentGameName)
        with(reversiScope) {
            GameCard(
                game = game,
                cardData = cardState,
                enabled = cardState != CardStatus.CORRUPTED,
                modifier = Modifier
                    .width(maxCardWidth)
                    .height(maxCardHeight)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                        this.translationX =
                            if (pageOffset < 0) translation.toPx() else -translation.toPx()
                    },
                onClick = { onGameClick(game, page) },
            )
        }
    }
}

