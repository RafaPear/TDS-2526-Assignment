package pt.isel.reversi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.exceptions.ErrorMessage
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.setPage

/**
 * Default previous page button that navigates back to the stored back page.
 *
 * @param appState Global application state for navigation.
 */
@Composable
fun ReversiScope.previousPageContentDefault(appState: MutableState<AppState>) {
    PreviousPage {
        appState.setPage(appState.value.backPage)
    }
}

/**
 * Main scaffold composable providing consistent layout structure for pages.
 * Includes top app bar with title and navigation, content area, and error handling.
 *
 * @param appState Global application state for navigation and theming.
 * @param backgroundTopBar Background color for the top app bar.
 * @param title Title text displayed in the top app bar.
 * @param loadingModifier Modifier for the loading state.
 * @param previousPageContent Optional custom previous page navigation button.
 * @param content Main content composable lambda.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScaffoldView(
    appState: MutableState<AppState>,
    backgroundTopBar: Color = Color.Transparent,
    title: String = "",
    loadingModifier: Modifier = Modifier,
    previousPageContent: (@Composable ReversiScope.() -> Unit)? = null,
    content: @Composable ReversiScope.(paddingValues: PaddingValues) -> Unit
) {
    val theme = appState.value.theme
    val scope = ReversiScope(appState.value)
    Scaffold(modifier = Modifier.background(theme.backgroundColor), containerColor = Color.Transparent, topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundTopBar,
            ),
            title = {
                with(scope) {
                    ReversiText(
                        text = title,
                        color = theme.textColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = 50.sp
                        ),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            },
            navigationIcon = {
                if (previousPageContent != null) scope.previousPageContent()
                else scope.previousPageContentDefault(appState)
            }
        )
    }, snackbarHost = { appState.value.error?.let { scope.ErrorMessage(appState) } }
    ) { paddingValues ->
        with(scope) {
            Box {
                content(paddingValues)
                if (appState.value.isLoading) {
                    Loading(loadingModifier)
                }
            }
        }
    }
}