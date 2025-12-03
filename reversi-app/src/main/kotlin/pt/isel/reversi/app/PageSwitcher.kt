package pt.isel.reversi.app

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page

/**
 * Componente central que gere transições entre páginas.
 * Desliza para a direita (forward) ou esquerda (backward).
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppScreenSwitcher(appState: MutableState<AppState>, switchAction: @Composable (BoxScope.(page: Page) -> Unit)) {
    val targetPage = appState.value.page

    AnimatedContent(
        targetState = targetPage,
        transitionSpec = {
            val forward = targetPage.level > initialState.level

            val duration = 500
            val iOSEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

            if (forward) reversiGoInAnimation(duration, iOSEasing)
            else reversiGoOutAnimation(duration, iOSEasing)
        },
        modifier = Modifier.fillMaxSize().background(MAIN_BACKGROUND_COLOR),
        label = "PageTransition"
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (page != appState.value.backPage || appState.value.page == Page.MAIN_MENU) switchAction(page)
        }
    }
}

fun reversiGoInAnimation(
    duration: Int = 500,
    animation: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f),
): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(duration, easing = animation)
    ) + fadeIn(tween(duration, easing = animation)) togetherWith
    slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(duration, easing = animation)
    ) + fadeOut(tween(duration, easing = animation))

fun reversiGoOutAnimation (
    duration: Int = 500,
    animation: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(duration, easing = animation)
    ) + fadeIn(tween(duration, easing = animation)) togetherWith
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(duration, easing = animation)
    ) + fadeOut(tween(duration, easing = animation))

fun reversiFadeAnimation(
    duration: Int = 500,
    animation: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
): ContentTransform =
    fadeIn(tween(duration, easing = animation)) togetherWith
    fadeOut(tween(duration, easing = animation))
