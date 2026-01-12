package pt.isel.reversi.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import pt.isel.reversi.app.exceptions.ErrorMessage
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.core.exceptions.ReversiException


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
fun ReversiScope.ScaffoldView(
    setError: (Exception?) -> Unit,
    backgroundTopBar: Color = Color.Transparent,
    error: ReversiException?,
    isLoading: Boolean = false,
    title: String = "",
    loadingModifier: Modifier = Modifier,
    previousPageContent: @Composable ReversiScope.() -> Unit,
    content: @Composable ReversiScope.(paddingValues: PaddingValues) -> Unit,
) {
    val theme = appState.theme
    val backgroundImage = theme.backgroundImage

    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer

        // Scaffold layer
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundTopBar,
                    ),
                    title = {
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
                    },
                    navigationIcon = {
                        if (!isLoading && appState.page != Page.MAIN_MENU) {
                            previousPageContent()
                        }
                    }
                )
            },
            snackbarHost = { ErrorMessage(error) { setError(it) } }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                backgroundImage?.let { imageRes ->
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.2f),
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
            Box {
                content(paddingValues)
                if (isLoading) {
                    Loading(loadingModifier)
                }
            }
        }
    }
}