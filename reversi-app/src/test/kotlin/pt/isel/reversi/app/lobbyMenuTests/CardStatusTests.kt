package pt.isel.reversi.app.lobbyMenuTests

import pt.isel.reversi.app.pages.lobby.LobbyLoadedState
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.CardStatus
import pt.isel.reversi.app.pages.lobby.lobbyViews.lobbyCarousel.getCardStatus
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.gameServices.EmptyGameService
import pt.isel.reversi.core.gameState.GameState
import pt.isel.reversi.core.gameState.MatchPlayers
import pt.isel.reversi.core.gameState.Player
import pt.isel.reversi.utils.BASE_FOLDER
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CardStatusTests {

    @BeforeTest
    @AfterTest
    fun cleanUp() {
        File(BASE_FOLDER).deleteRecursively()
    }

    @Test
    fun `getCardStatus returns CURRENT_GAME for current game`() {
        val gameState = GameState(
            board = Board(4).startPieces(),
            players = MatchPlayers(Player(PieceType.BLACK)),
            lastPlayer = PieceType.WHITE
        )
        val game = Game(currGameName = "testGame", gameState = gameState, service = EmptyGameService())
        val lobbyStats = LobbyLoadedState(game.gameState!!, game.currGameName!!)

        val expected = CardStatus.CURRENT_GAME

        val result = getCardStatus(lobbyStats, game.currGameName)
        assertEquals(expected, result)
    }

    @Test
    fun `getCardStatus returns FULL for game with 2 players`() {
        val gameState = GameState(
            board = Board(4).startPieces(),
            players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            lastPlayer = PieceType.WHITE
        )
        val game = Game(currGameName = "testGame", gameState = gameState, service = EmptyGameService())
        val lobbyStats = LobbyLoadedState(game.gameState!!, game.currGameName!!)
        val expected = CardStatus.FULL

        val result = getCardStatus(lobbyStats, "anotherGame")
        assertEquals(expected, result)
    }

    @Test
    fun `getCardStatus returns WAITING_FOR_PLAYERS for game with one player`() {
        val gameState = GameState(
            board = Board(4).startPieces(),
            players = MatchPlayers(Player(PieceType.BLACK)),
            lastPlayer = PieceType.WHITE
        )
        val game = Game(currGameName = "testGame", gameState = gameState, service = EmptyGameService())
        val lobbyStats = LobbyLoadedState(game.gameState!!, game.currGameName!!)
        val expected = CardStatus.WAITING_FOR_PLAYERS

        val result = getCardStatus(lobbyStats, "anotherGame")
        assertEquals(expected, result)
    }

    @Test
    fun `getCardStatus returns EMPTY for game with 0 players`() {
        val gameState = GameState(
            board = Board(4).startPieces(),
            players = MatchPlayers(),
            lastPlayer = PieceType.BLACK
        )
        val game = Game(currGameName = "testGame", gameState = gameState, service = EmptyGameService())
        val lobbyStats = LobbyLoadedState(game.gameState!!, game.currGameName!!)
        val expected = CardStatus.EMPTY

        val result = getCardStatus(lobbyStats, "anotherGame")
        assertEquals(expected, result)
    }
}