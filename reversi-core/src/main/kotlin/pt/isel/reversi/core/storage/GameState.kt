package pt.isel.reversi.core.storage

import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType

/**
 * Represents the state of a Reversi game, including the last player who made a move and the current board configuration.
 * @property lastPlayer The type of the last player who made a move, or null if no moves have been made.
 * @property board The current state of the game board, or null if the board is not initialized.
 * @property players The list of players in the game.
 * @property winner The player who has won the game, or null if there is no winner
 */
data class GameState(
    val players: MatchPlayers,
    val lastPlayer: PieceType,
    val board: Board,
    val winner: Player? = null,
) {
    fun refreshPlayers(): GameState =
        copy(players = players.refreshPlayers(board))

    fun changeName(newName: String, pieceType: PieceType): GameState {
        val player = players.getPlayerByType(pieceType) ?: return this
        return copy(
            players = players.copy(
                player1 = if (players.player1 == player)
                    player.copy(name = newName)
                else
                    players.player1,
                player2 = if (players.player2 == player)
                    player.copy(name = newName)
                else
                    players.player2
            )
        )
    }
}