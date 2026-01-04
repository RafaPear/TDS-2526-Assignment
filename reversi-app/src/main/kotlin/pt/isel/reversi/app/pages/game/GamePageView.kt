package pt.isel.reversi.app.pages.game

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import pt.isel.reversi.app.pages.game.utils.DrawBoard
import pt.isel.reversi.app.pages.game.utils.TargetButton
import pt.isel.reversi.app.pages.game.utils.TextPlayersScore
import pt.isel.reversi.app.pages.game.utils.testTagGamePage
import pt.isel.reversi.core.Game
import pt.isel.reversi.core.board.Coordinate

@Composable
fun GamePageView(
    modifier: Modifier = Modifier,
    game: Game,
    freeze: Boolean,
    getAvailablePlays: () -> List<Coordinate>,
    onCellClick: (coordinate: Coordinate) -> Unit,
    setTargetMode: (target: Boolean) -> Unit
) {
    Column(
        modifier = modifier.testTag(tag = testTagGamePage()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
        ) {
            Box(
                modifier = Modifier.weight(0.7f),
            ) {
                val gameState = game.gameState ?: return@Box
                DrawBoard(
                    game.target,
                    gameState,
                    freeze = freeze,
                    getAvailablePlays = { getAvailablePlays() }
                ) { onCellClick(it) }
            }

            Spacer(modifier = Modifier.width(width = 16.dp))

            // Coluna dos jogadores e botões
            Column(
                modifier = Modifier.weight(0.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextPlayersScore(state = game.gameState)

                val target = game.target

                Spacer(modifier = Modifier.height(32.dp))

                TargetButton(target, freeze = freeze) { setTargetMode(!target) }
            }
        }
    }
}