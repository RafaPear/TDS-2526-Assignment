package pt.isel.reversi.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.exceptions.ErrorMessage
import pt.isel.reversi.app.gamePage.TEXT_COLOR
import pt.isel.reversi.app.state.AppState


//**
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScaffoldView(
    appState: MutableState<AppState>,
    backgroundTopBar: Color = Color.Transparent,
    title: String = "",
    previousPageContent: @Composable () -> Unit,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundTopBar,
                ),
                title = {
                    Text(
                        text = title,
                        color = TEXT_COLOR,
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
                    previousPageContent()
                },
            )
        },
        snackbarHost = { appState.value.error?.let { ErrorMessage(appState) } }
    ) { paddingValues ->
        content(paddingValues)
    }
}