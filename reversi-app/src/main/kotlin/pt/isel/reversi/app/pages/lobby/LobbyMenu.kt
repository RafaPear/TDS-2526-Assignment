package pt.isel.reversi.app.pages.lobby

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.state.AppState
import pt.isel.reversi.app.state.Page
import pt.isel.reversi.app.state.setGame
import pt.isel.reversi.app.state.setPage
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.loadGame
import pt.isel.reversi.core.readGame
import pt.isel.reversi.utils.LOGGER
import kotlin.math.absoluteValue

// Cores
private val BOARD_COLOR = Color(0xFF2E7D32)
private val BOARD_BORDER = Color(0xFF1B5E20)
private val PRIMARY = Color(0xFF1976D2)
private val BACKGROUND = Color(0xFF121212)
private val CARD_BG = Color(0xFF1E1E1E)

// =============================================================================
// Preview do Tabuleiro
// =============================================================================
@Composable
fun BoardPreview(board: Board, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp))
            .background(BOARD_COLOR, RoundedCornerShape(12.dp)).border(3.dp, BOARD_BORDER, RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            repeat(board.side) { y ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    repeat(board.side) { x ->
                        val piece = board[Coordinate(x + 1, y + 1)]
                        Cell(piece, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(piece: PieceType?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f).padding(0.5.dp).background(BOARD_COLOR), contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                val radius = size.minDimension / 2 * 0.7f
                val center = Offset(size.width / 2, size.height / 2)
                val color = if (piece == PieceType.BLACK) Color.Black else Color.White

                // Sombra
                drawCircle(Color.Black.copy(0.3f), radius * 1.1f, center + Offset(2f, 2f))
                // Lateral 3D
                val sideColor = if (color == Color.White) Color(0xFFCFD8DC) else Color(0xFF37474F)
                drawCircle(sideColor, radius, center + Offset(1f, 1f))
                // Peça
                drawCircle(color, radius * 0.95f, center)
                // Highlight
                drawCircle(
                    Color.White.copy(if (color == Color.White) 0.6f else 0.35f),
                    radius * 0.25f,
                    center - Offset(radius * 0.35f, radius * 0.35f)
                )
            }
        }
    }
}

enum class GameStatus(val text: String, val color: Color) {
    IN_PROGRESS("Em Progresso", Color.Green),
    WAITING_FOR_PLAYERS("Aguardando Jogadores", Color.Yellow),
    FULL("Cheio", Color.Blue),
    CORRUPTED("Corrompido", Color.Red),
    CURRENT_GAME("Jogo Atual", Color.Cyan)
}

// =============================================================================
// Card do Jogo
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCard(
    game: Game, enabled: Boolean, statusData: GameStatus, scale: Float, maxWidth: Dp, maxHeight: Dp, onClick: () -> Unit
) {
    val name = game.currGameName ?: return
    val state = game.gameState ?: return

    // Lógica corrigida dos estados
    val statusText = statusData.text
    val statusColor = statusData.color

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(maxWidth).height(maxHeight).graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = 0.4f + (scale - 0.85f) * 4f
        }.shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1E1E), Color(0xFF2D2D2D))
                )
            ).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                Modifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(statusText, statusColor)
            }

            Row(Modifier.weight(3f)) {

                // Tabuleiro
                BoardPreview(
                    board = state.board, modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Row(Modifier.fillMaxWidth().weight(2f)) {
                // Placar - sempre mostra as pontuações
                ScorePanel(state.board)
            }

            Row(Modifier.weight(1f)) {

                if (state.players.isNotEmpty()) {// Botão
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp), color = color.copy(0.2f)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun ScorePanel(board: Board) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreItem(PieceType.BLACK, board.totalBlackPieces)
        ScoreItem(PieceType.WHITE, board.totalWhitePieces)
    }
}

@Composable
fun ScoreItem(type: PieceType, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(40.dp).background(
                if (type == PieceType.BLACK) Color.Black else Color.White, CircleShape
            ).border(
                2.dp, if (type == PieceType.BLACK) Color.White.copy(0.3f) else Color.Black.copy(0.2f), CircleShape
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = score.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White
        )
        Text(
            text = if (type == PieceType.BLACK) "Preto" else "Branco", fontSize = 11.sp, color = Color.White.copy(0.7f)
        )
    }
}

// =============================================================================
// Carrossel com HorizontalPager
// =============================================================================
// =============================================================================
// Carrossel com HorizontalPager - CORRIGIDO
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCarousel(appState: MutableState<AppState>, games: List<Game>, onGameClick: (Game) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { games.size })
    val scope = rememberCoroutineScope()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val availableWidth = this.maxWidth
        val availableHeight = this.maxHeight

        // 1. DEFINIR TAMANHOS MÁXIMOS DO CARTÃO PRIMEIRO (CORREÇÃO)
        val maxCardWidth = (availableWidth * 0.7f).coerceAtMost(450.dp)
        val maxCardHeight = (availableHeight * 0.7f).coerceAtMost(650.dp)

        // 2. CALCULAR PADDING USANDO O TAMANHO DEFINIDO
        val minHorizontalPadding = 32.dp

        // Calcula o padding necessário para centralizar o cartão de largura maxCardWidth
        val horizontalPadding = (availableWidth / 2 - maxCardWidth / 2).coerceAtLeast(minHorizontalPadding)

        // Pager
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            HorizontalPager(
                state = pagerState, modifier = Modifier.fillMaxSize(),
                // Usa o padding calculado
                contentPadding = PaddingValues(horizontal = horizontalPadding), pageSpacing = 16.dp
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val scale = 0.85f + (1f - pageOffset.absoluteValue.coerceIn(0f, 1f)) * 0.15f

                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    val game = remember(games[page]) { mutableStateOf(games[page]) }
                    val statusData = remember(game.value) { mutableStateOf(GameStatus.WAITING_FOR_PLAYERS) }
                    val scope = rememberCoroutineScope()

                    scope.launch {
                        while (isActive) {
                            try {
                                game.value = game.value.hardRefresh()
                                val state = game.value.gameState ?: throw Exception("Estado do jogo nulo")
                                statusData.value = when {
                                    appState.value.game.currGameName == game.value.currGameName -> GameStatus.CURRENT_GAME
                                    state.players.size == 2                                     -> GameStatus.IN_PROGRESS
                                    state.players.size == 1                                     -> GameStatus.WAITING_FOR_PLAYERS
                                    state.players.isEmpty()                                     -> GameStatus.FULL
                                    else                                                        -> GameStatus.CORRUPTED
                                }
                            } catch (e: Exception) {
                                statusData.value = GameStatus.CORRUPTED
                                LOGGER.warning("Erro ao atualizar jogo: ${game.value.currGameName} - ${e.message}")
                            }
                            val delay = when (statusData.value) {
                                GameStatus.IN_PROGRESS         -> 100L
                                GameStatus.WAITING_FOR_PLAYERS -> 500L
                                GameStatus.FULL                -> 15000L
                                GameStatus.CORRUPTED           -> 20000L
                                GameStatus.CURRENT_GAME        -> 100L
                            }
                            delay(delay)
                        }
                    }

                    GameCard(
                        game = game.value,
                        statusData = statusData.value,
                        scale = scale,
                        maxWidth = maxCardWidth,
                        maxHeight = maxCardHeight,
                        enabled = statusData.value !in listOf(GameStatus.CORRUPTED, GameStatus.FULL),
                        onClick = {
                            if (page == pagerState.currentPage) {
                                onGameClick(games[page])
                            } else {
                                scope.launch { pagerState.animateScrollToPage(page) }
                            }
                        })
                }
            }
        }

        // Navegação (mantida)
        if (games.size > 1) {
            if (pagerState.currentPage > 0) {
                NavButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    alignment = Alignment.CenterStart,
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } })
            }
            if (pagerState.currentPage < games.size - 1) {
                NavButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    alignment = Alignment.CenterEnd,
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } })
            }
        }

        // Indicadores (mantidos)
        Column(
            Modifier.align(Alignment.TopCenter).padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicators(games.size, pagerState.currentPage)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${pagerState.currentPage + 1} de ${games.size}",
                fontSize = 14.sp,
                color = Color.White.copy(0.6f)
            )
        }
    }
}

@Composable
fun BoxScope.NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, alignment: Alignment, onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.align(alignment).padding(16.dp).size(50.dp)
            .background(Color(0xFF2D2D2D).copy(0.9f), CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape)
    ) {
        val icons = listOf(
            Icons.AutoMirrored.Rounded.ArrowBackIos, Icons.AutoMirrored.Rounded.ArrowForwardIos
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
                if (index == current) 28.dp else 8.dp, spring(Spring.DampingRatioMediumBouncy), label = "indicator"
            )
            Box(
                Modifier.width(width).height(8.dp).clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(if (index == current) 1f else 0.3f))
            )
        }
    }
}

// =============================================================================
// Menu Principal
// =============================================================================
@Composable
fun LobbyMenu(appState: MutableState<AppState>, modifier: Modifier = Modifier) {
    val games = remember { mutableStateListOf<Game>() }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    scope.launch {
        isLoading = true
        val ids = appState.value.game.getAllSavedGames()
        games.clear()
        val loaded = ids.mapNotNull { id ->
            try {
                readGame(id)
            } catch (e: Exception) {
                LOGGER.warning("Erro ao ler jogo: $id")
                null
            }
        }
        games.addAll(loaded)
        isLoading = false
    }

    ScaffoldView(appState, title = "Lobby - Jogos Guardados") { padding ->
        Box(
            Modifier.fillMaxSize().background(BACKGROUND).padding(padding)
        ) {
            when {
                isLoading       -> Loading()
                games.isEmpty() -> Empty()
                else            -> GameCarousel(appState, games) { game ->
                    LOGGER.info("Jogo selecionado: ${game.currGameName}")
                    val state = game.gameState
                    if (state == null) {
                        LOGGER.warning("Estado do jogo nulo para o jogo: ${game.currGameName}")
                        return@GameCarousel
                    }
                    if (state.players.isEmpty()) {
                        LOGGER.warning("Jogo cheio selecionado: ${game.currGameName}")
                        return@GameCarousel
                    }

                    val name = game.currGameName
                    if (name == null) {
                        LOGGER.warning("Nome do jogo nulo ao tentar entrar no jogo.")
                        return@GameCarousel
                    }
                    val appGame = appState.value.game
                    scope.launch {
                        try {
                            appGame.saveEndGame()
                        } catch (e: Exception) {
                            LOGGER.warning("Erro ao salvar estado atual do jogo: ${e.message}")
                        }
                    }
                    val joinedGame = runBlocking { loadGame(name) }
                    appState.value = setGame(appState, joinedGame)
                    appState.value = setPage(appState, Page.GAME)
                    appState.value = appState.value.copy(backPage = Page.LOBBY)
                }
            }
        }
    }
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
            Icon(Icons.Filled.SportsEsports, null, Modifier.size(80.dp), Color.White.copy(0.3f))
            Spacer(Modifier.height(16.dp))
            Text("Nenhum jogo guardado", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Comece um novo jogo", fontSize = 14.sp, color = Color.White.copy(0.6f), textAlign = TextAlign.Center)
        }
    }
}

// Test tags
fun testTagLobbyBoard() = "LobbyBoardPreview"
fun testTagCellPreview(coordinateIndex: Int) = "LobbyCellPreview_$coordinateIndex"
fun testTagCarouselItem(name: String) = "LobbyCarouselItem_$name"