package pt.isel.reversi.app.pages.lobby

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.HIT_SOUND
import pt.isel.reversi.app.MAIN_BACKGROUND_COLOR
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.exceptions.GameCorrupted
import pt.isel.reversi.app.exceptions.GameIsFull
import pt.isel.reversi.app.reversiFadeAnimation
import pt.isel.reversi.app.state.*
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.ErrorType
import pt.isel.reversi.core.getAllGameNames
import pt.isel.reversi.core.loadGame
import pt.isel.reversi.core.readGame
import pt.isel.reversi.utils.LOGGER
import kotlin.math.absoluteValue

private val PRIMARY = Color(0xFF1976D2)
private val BACKGROUND = Color(0xFF121212)

enum class GameStatus(val text: String, val color: Color) {
    EMPTY("Vazio", Color.Green),
    WAITING_FOR_PLAYERS("Aguardando Jogadores", Color.Yellow),
    FULL("Cheio", Color.Blue),
    CORRUPTED("Corrompido", Color.Red),
    CURRENT_GAME("Jogo Atual", Color.Cyan)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LobbyCarousel(
    appState: MutableState<AppState>,
    games: List<Game>,
    onGameClick: (Game) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { games.size })
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {


        OutlinedTextField(
            value = search,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White.copy(alpha = 0.7f),
                unfocusedTextColor = Color.Gray.copy(alpha = 0.7f),
                cursorColor = Color.White.copy(alpha = 0.7f),
                focusedBorderColor = PRIMARY,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            onValueChange = {
                scope.launch {
                    search = it
                    val game = games.find { game -> game.currGameName == it } ?: return@launch
                    val page = games.indexOf(game)
                    pagerState.animateScrollToPage(
                        page = page,
                        animationSpec = spring(
                            stiffness = 400f,
                            dampingRatio = 0.75f
                        )
                    )
                }
            },
            placeholder = { Text("Procure um jogo...") },
        )

        BoxWithConstraints {
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

                val gameState = remember { mutableStateOf(games[page]) }
                val statusState = remember { mutableStateOf(GameStatus.WAITING_FOR_PLAYERS) }

                LaunchedEffect(gameState.value.currGameName) {
                    while (isActive) {
                        try {
                            gameState.value = gameState.value.hardRefresh()
                            val state =
                                gameState.value.gameState ?: throw Exception("Estado do jogo nulo")

                            statusState.value = when {
                                appState.value.game.currGameName == gameState.value.currGameName ->
                                    GameStatus.CURRENT_GAME

                                state.players.size == 2 -> GameStatus.EMPTY
                                state.players.size == 1 -> GameStatus.WAITING_FOR_PLAYERS
                                state.players.isEmpty() -> GameStatus.FULL
                                else -> GameStatus.CORRUPTED
                            }
                        } catch (e: Exception) {
                            statusState.value = GameStatus.CORRUPTED
                            LOGGER.warning(
                                "Erro ao atualizar jogo: ${gameState.value.currGameName} - ${e.message}"
                            )
                        }

                        val delayMillis = when (statusState.value) {
                            GameStatus.EMPTY -> 100L
                            GameStatus.WAITING_FOR_PLAYERS -> 500L
                            GameStatus.FULL -> 15_000L
                            GameStatus.CORRUPTED -> 20_000L
                            GameStatus.CURRENT_GAME -> 100L
                        }
                        delay(delayMillis)

                    }
                }

                GameCard(
                    game = gameState.value,
                    statusData = statusState.value,
                    enabled = statusState.value !in listOf(
                        GameStatus.CORRUPTED,
                        GameStatus.FULL
                    ),
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
                    onClick = {
                        scope.launch {
                            if (page != pagerState.currentPage) {
                                pagerState.animateScrollToPage(
                                    page,
                                    animationSpec = spring(
                                        stiffness = 400f,
                                        dampingRatio = 0.75f
                                    )
                                )
                            }
                            delay(150L)
                            onGameClick(gameState.value)
                        }
                    }
                )
            }

            if (games.size > 1) {
                if (pagerState.currentPage > 0) {
                    NavButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        alignment = Alignment.CenterStart,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    )
                }
                if (pagerState.currentPage < games.size - 1) {
                    NavButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        alignment = Alignment.CenterEnd,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageIndicators(games.size, pagerState.currentPage)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${pagerState.currentPage + 1} de ${games.size}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun BoxScope.NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    alignment: Alignment,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .align(alignment)
            .padding(16.dp)
            .size(50.dp)
            .background(Color(0xFF2D2D2D).copy(alpha = 0.9f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    ) {
        val icons = listOf(
            Icons.AutoMirrored.Rounded.ArrowBackIos,
            Icons.AutoMirrored.Rounded.ArrowForwardIos
        )
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = if (icon in icons) Modifier.padding(start = 4.dp) else Modifier
        )
    }
}

@Composable
fun PageIndicators(total: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { index ->
            val width by animateDpAsState(
                targetValue = if (index == current) 28.dp else 8.dp,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                label = "indicator"
            )
            Box(
                Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Color.White.copy(
                            alpha = if (index == current) 1f else 0.3f
                        )
                    )
            )
        }
    }
}

private enum class LobbyState {
    LOADING, EMPTY, BACKGROUND_DRAW, SHOW_GAMES
}


@Composable
fun LobbyMenu(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val games = remember { mutableStateListOf<Game>() }
    val lobbyState = remember { mutableStateOf(LobbyState.EMPTY) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) {
        while (isActive) {
            val ids = getAllGameNames()
            val hasNewId = ids.any { id -> id !in games.mapNotNull { it.currGameName } } ||
                    games.size != ids.size

            if (hasNewId) {
                lobbyState.value = LobbyState.LOADING
                delay(100L)
                val loaded = ids.mapNotNull { id ->
                    try {
                        runBlocking { readGame(id) }
                    } catch (e: Exception) {
                        LOGGER.warning("Erro ao ler jogo: $id - ${e.message}")
                        null
                    }
                }
                games.clear()
                games.addAll(loaded)
                lobbyState.value = if (games.isEmpty()) LobbyState.EMPTY else LobbyState.BACKGROUND_DRAW
            }
            delay(1000L)
        }
    }

    ScaffoldView(appState, title = "Lobby - Jogos Guardados") { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(BACKGROUND)
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = lobbyState.value,
                transitionSpec = {
                    val duration = 500
                    val iOSEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                    reversiFadeAnimation(duration, iOSEasing)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MAIN_BACKGROUND_COLOR),
                label = "PageTransition"
            ) { page ->
                when (page) {
                    LobbyState.LOADING -> Loading()
                    LobbyState.EMPTY -> Empty()
                    LobbyState.BACKGROUND_DRAW -> LobbyBackgroundLoad(lobbyState)
                    LobbyState.SHOW_GAMES -> LobbyCarousel(appState, games) { game ->
                        selectedGame = game
                    }
                }
            }

            selectedGame?.let { game ->
                LobbyLoadGame(
                    appState = appState,
                    game = game,
                    onClose = { selectedGame = null }
                )
            }
        }
    }
}

@Composable
private fun LobbyBackgroundLoad(lobbyState: MutableState<LobbyState>) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MAIN_BACKGROUND_COLOR)
    ) {
        LaunchedEffect(Unit) {
            delay(100L)
            lobbyState.value = LobbyState.SHOW_GAMES
        }
    }
}

@Composable
private fun LobbyLoadGame(
    appState: MutableState<AppState>,
    game: Game,
    onClose: () -> Unit
) {
    LOGGER.info("Jogo selecionado: ${game.currGameName}")
    val state = game.gameState
    val name = game.currGameName

    if (name == appState.value.game.currGameName) {
        appState.setPage(Page.GAME)
        appState.value = appState.value.copy(backPage = Page.LOBBY)
        onClose()
        return
    }

    if (state == null) {
        LOGGER.warning("Estado do jogo nulo para o jogo: ${game.currGameName}")
        appState.setError(
            GameCorrupted(
                message = "O jogo '${game.currGameName}' está corrompido.",
                type = ErrorType.ERROR
            )
        )
        onClose()
        return
    }
    if (state.players.isEmpty()) {
        LOGGER.warning("Jogo cheio selecionado: ${game.currGameName}")
        appState.setError(GameIsFull())
        onClose()
        return
    }

    if (name == null) {
        LOGGER.warning("Nome do jogo nulo ao tentar entrar no jogo.")
        appState.setError(
            GameCorrupted(
                message = "O jogo selecionado não tem um nome válido.",
                type = ErrorType.ERROR
            )
        )
        onClose()
        return
    }


    val players = state.players.map { it.type }
    PickAPiece(
        pieces = players,
        onPick = { pieceType ->
            val gameName = game.currGameName ?: return@PickAPiece
            val appGame = appState.value.game

            val joinedGame = runBlocking {
                try {
                    appGame.saveEndGame()
                } catch (e: Exception) {
                    LOGGER.warning("Erro ao salvar estado atual do jogo: ${e.message}")
                }
                try {
                    loadGame(gameName, pieceType)
                } catch (e: Exception) {
                    appState.setError(e)
                    null
                }
            }

            if (joinedGame == null) {
                onClose()
                return@PickAPiece
            }

            appState.getStateAudioPool().play(HIT_SOUND)
            appState.setGame(joinedGame)
            appState.setPage(Page.GAME)
            appState.value = appState.value.copy(backPage = Page.LOBBY)
            onClose()
        },
        onDismiss = {
            appState.setPage(Page.LOBBY)
            onClose()
        }
    )
}

@Composable
fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PRIMARY, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("A carregar jogos...", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun Empty() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Nenhum jogo guardado",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Comece um novo jogo",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PickAPiece(
    pieces: List<PieceType>,
    onPick: (PieceType) -> Unit,
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Escolha a sua peça",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    pieces.forEach { piece ->
                        val color = when (piece) {
                            PieceType.BLACK -> Color.Black
                            PieceType.WHITE -> Color.White
                        }
                        val borderColor = when (piece) {
                            PieceType.BLACK -> Color.White.copy(alpha = 0.3f)
                            PieceType.WHITE -> Color.Black.copy(alpha = 0.2f)
                        }
                        IconButton(
                            onClick = { onPick(piece) },
                            modifier = Modifier
                                .size(80.dp)
                                .background(color, CircleShape)
                                .border(2.dp, borderColor, CircleShape)
                        ) {
                        }
                    }
                }
            }
        }
    }
}

// Test tags
fun testTagLobbyBoard() = "LobbyBoardPreview"
fun testTagCellPreview(coordinateIndex: Int) = "LobbyCellPreview_$coordinateIndex"
fun testTagCarouselItem(name: String) = "LobbyCarouselItem_$name"
