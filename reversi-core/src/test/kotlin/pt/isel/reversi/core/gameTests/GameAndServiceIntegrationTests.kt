package pt.isel.reversi.core.gameTests

import pt.isel.reversi.core.*
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.InvalidGameException
import pt.isel.reversi.core.exceptions.InvalidNameAlreadyExists
import pt.isel.reversi.core.exceptions.InvalidPlayException
import pt.isel.reversi.core.gameServices.GameService
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.GameStorageType
import pt.isel.reversi.core.storage.MatchPlayers
import pt.isel.reversi.core.storage.StorageParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameAndServiceIntegrationTests {
    val gameService = GameService(
        storage = GameStorageType.FILE_STORAGE,
        params = StorageParams.FileStorageParams(folder = "data/saves")
    )
    @Test
    fun `startNewGame with already existing name in storage fails`() {
        cleanup {
            startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "existingGame",
                service = gameService,
            )

            assertFailsWith<InvalidNameAlreadyExists> {
                startNewGame(
                    side = 4,
                    players = MatchPlayers(Player(PieceType.WHITE)),
                    firstTurn = PieceType.WHITE,
                    currGameName = "existingGame",
                    service = gameService,
                )
            }
        }
    }

    @Test
    fun `pass no local game with 2 players succeeds`() {
        cleanup {
            var uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                currGameName = "testGame",
                firstTurn = PieceType.BLACK,
                service = gameService,
            )
            val gameState = uut.gameState!!.copy(
                board = Board(4)
                    .addPiece(Piece(Coordinate(1, 1), PieceType.BLACK))
                    .addPiece(Piece(Coordinate(1, 2), PieceType.BLACK))
            )
            uut.saveOnlyBoard(gameState)
            uut = uut.refresh()

            var uut2 = loadAndEntryGame(
                gameName = uut.currGameName!!,
                desiredType = null,
                service = gameService,
            )

            uut.pass()
            uut2 = uut2.refresh()


            uut2 = uut2.pass()

            assertEquals(Player(PieceType.BLACK, points = 2), uut2.gameState?.winner)

            uut = uut.refresh()

            assertEquals(Player(PieceType.BLACK, points = 2), uut.gameState?.winner)
        }
    }

    @Test
    fun `startNewGame and load game with 1 player succeeds`() {
        cleanup {
            val player1 = Player(PieceType.BLACK, points = 2)
            val expectedPlayers = MatchPlayers(player1)
            val expectedBoard = Board(4).startPieces()
            val expectedLastPlayer = PieceType.WHITE

            val game = startNewGame(
                side = 4,
                players = MatchPlayers(player1),
                firstTurn = PieceType.BLACK,
                currGameName = "existingGame",
                service = gameService,
            )


            val loadedGame = game.service.hardLoad("existingGame")?.let {
                Game(
                    target = false,
                    gameState = it,
                    currGameName = "existingGame",
                    service = gameService,
                )
            }!!

            assertEquals(expectedBoard, loadedGame.gameState?.board)
            assertEquals(expectedPlayers, loadedGame.gameState?.players)
            assertEquals(expectedLastPlayer, loadedGame.gameState?.lastPlayer)
        }
    }


    @Test
    fun `pass in not local game with available plays fails`() {
        cleanup {
            var uutB = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
                service = gameService,
            )

            var uutW = loadAndEntryGame(
                gameName = "testGame",
                desiredType = null,
                service = gameService,
            )

            uutB = uutB.refresh()
            assertFailsWith<InvalidPlayException> {
                uutB.pass()
            }

            assertEquals(PieceType.WHITE, uutB.gameState?.lastPlayer)

            uutW = uutW.refresh()
            assertFailsWith<InvalidPlayException> {
                uutW.pass()
            }

            assertEquals(PieceType.WHITE, uutW.gameState?.lastPlayer)
        }
    }

    @Test
    fun `refresh in not local game loads updated game state`() {
        cleanup {
            var uutB = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.WHITE,
                currGameName = "testGame",
                service = gameService,
            )

            var uutW = loadAndEntryGame(
                gameName = "testGame",
                desiredType = null,
                service = gameService,
            )

            uutW = uutW.play(uutW.getAvailablePlays().first())

            uutB = uutB.refresh()
            assertEquals(uutW.gameState, uutB.gameState)
        }
    }

    @Test
    fun `saveEndGame in not local game succeeds`() {
        cleanup {
            val uut = startNewGame(
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.WHITE,
                currGameName = "testGame",
                side = 4,
                service = gameService,
            )

            uut.saveEndGame()

            val loadedGameState = uut.service.hardLoad("testGame")

            val expectedGameState = uut.gameState?.copy(
                players = MatchPlayers(Player(PieceType.BLACK)).refreshPlayers(uut.gameState.board),
            )

            assertEquals(expectedGameState, loadedGameState)
        }
    }

    @Test
    fun `saveOnlyBoard in not local game, verify if my name is preserved when my name is changed`() {
        cleanup {
            val initialMyPlayer = Player(PieceType.BLACK, name = "Player 1")
            var initialPlayer2 = Player(PieceType.WHITE, name = "Player 2")
            var uut = startNewGame(
                side = 4,
                players = MatchPlayers(initialMyPlayer, initialPlayer2),
                firstTurn = initialMyPlayer.type,
                currGameName = "testGame",
                service = gameService,
            )

            val expectedMyPlayer = initialMyPlayer.copy(name = "Changed Name").refresh(uut.gameState!!.board) // Refresh points
            initialPlayer2 = initialPlayer2.refresh(uut.gameState.board) // Refresh points

            uut = uut.copy(
                gameState = uut.gameState.changeName(
                    newName = expectedMyPlayer.name,
                    pieceType = expectedMyPlayer.type
                )
            )

            uut.saveOnlyBoard(uut.gameState)

            val lsGameState = uut.service.hardLoad(uut.currGameName!!)

            assertEquals(expectedMyPlayer, lsGameState?.players?.getPlayerByType(expectedMyPlayer.type))
            assertEquals(initialPlayer2, lsGameState?.players?.getPlayerByType(initialPlayer2.type))
        }
    }

    @Test
    fun `saveEndGame with players empty fails`() {
        cleanup {
            val uut = Game(
                target = false,
                gameState = GameState(
                    players = MatchPlayers(),
                    lastPlayer = PieceType.BLACK,
                    board = Board(4).startPieces()
                ),
                currGameName = "testGame",
                service = gameService,
            )

            assertFailsWith<InvalidGameException> {
                uut.saveEndGame()
            }
        }
    }

    @Test
    fun `saveEndGame with game not started yet fails`() {
        cleanup {
            val game = Game(
                service = gameService,
            )

            assertFailsWith<InvalidGameException> {
                game.service.saveEndGame(game)
            }
        }
    }
}