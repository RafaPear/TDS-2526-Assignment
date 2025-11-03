package pt.isel.reversi.core

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.EndGameException
import pt.isel.reversi.core.exceptions.InvalidFileException
import pt.isel.reversi.core.exceptions.InvalidGameException
import pt.isel.reversi.core.exceptions.InvalidPlayException
import pt.isel.reversi.core.storage.GameState
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameTests {

    fun cleanup(func: () -> Unit) {
        File("saves").deleteRecursively()
        func()
        File("saves").deleteRecursively()
    }

    @Test
    fun `play with game not started yet`() {
        val game = Game()

        assertFailsWith<InvalidGameException> {
            game.play(Coordinate(1, 1))
        }
    }

    /*
          1 2 3 4
        1 . . . .
        2 . W B .
        3 . B W .
        4 . . . .
     */
    @Test
    fun `play with 1 player in local game and not his turn fails`() {
        val uut = startNewGame(
            side = 4,
            players = listOf(Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertFailsWith<InvalidPlayException> {
            uut.play(Coordinate(1, 2))
        }
    }

    /*
          1 2 3 4
        1 . . . .
        2 . W B .
        3 . B W .
        4 . . . .
     */
    @Test
    fun `play with 1 player in local game and is your turn succeeds`() {
        val expectedBoard = Board(4).startPieces()
            .addPiece(Coordinate(1, 2), PieceType.BLACK)
            .changePiece(Coordinate(2, 2))

        val uut = startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assert(expectedBoard == uut.play(Coordinate(1, 2)).gameState?.board)
    }

    @Test
            /*
          1 2 3 4
        1 . . . .
        2 . W B .
        3 . B W .
        4 . . . .
            */
    fun `play in local game with 2 players succeeds`() {
        val expectedBoard = Board(4).startPieces()
            .addPiece(Coordinate(1, 2), PieceType.BLACK)
            .changePiece(Coordinate(2, 2))

        val expectedGame = Game(
            gameState = GameState(
                lastPlayer = PieceType.BLACK,
                board = expectedBoard,
                players = listOf(
                    Player(PieceType.BLACK, 4),
                    Player(PieceType.WHITE, 1)
                )
            ),
            target = false,
            currGameName = null,
        )

        val uut = startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        ).play(Coordinate(1, 2))

        assert(expectedGame == uut)
    }

    @Test
            /*
          1 2 3 4
        1 . . . .
        2 . W W W
        3 . B B B
        4 . . . .
               */
    fun `play in local game verify players point update correctly and playerTurn after normal plays`() {
        val expectedBlackPoints = 3
        val expectedWhitePoints = 3
        val expectedPlayerTurn = PieceType.WHITE

        val uut = startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )
            .play(Coordinate(3, 4))
            .play(Coordinate(2, 4))

        assertEquals(expectedBlackPoints, uut.gameState?.players?.find { it.type == PieceType.BLACK }!!.points)
        assertEquals(expectedWhitePoints, uut.gameState.players.find { it.type == PieceType.WHITE }!!.points)
        assertEquals(expectedPlayerTurn, uut.gameState.lastPlayer)
    }

    @Test
    fun `play with players empty fails`() {
        val uut = Game(
            target = false,
            gameState = GameState(
                players = emptyList(),
                lastPlayer = PieceType.BLACK,
                board = Board(4).startPieces()
            ),
            currGameName = null,
        )

        assertFailsWith<InvalidGameException> {
            uut.play(Coordinate(1, 2))
        }
    }

    @Test
    fun `pass in localGame when no availablePlays succeeds`() {
        val uut = Game(
            target = false,
            gameState = GameState(
                players = listOf(
                    Player(PieceType.BLACK),
                    Player(PieceType.WHITE)
                ),
                board = Board(4)
                    .addPiece(Piece(Coordinate(1, 1), PieceType.BLACK))
                    .addPiece(Piece(Coordinate(1, 2), PieceType.BLACK)),
                lastPlayer = PieceType.BLACK,
            ),
            currGameName = null,
        ).pass()

        val expectedCountPass = 1
        val expectedPlayerTurn = PieceType.WHITE
        assertEquals(expectedCountPass, uut.countPass)
        assertEquals(expectedPlayerTurn, uut.gameState?.lastPlayer)

        assertFailsWith<EndGameException> {
            uut.pass()
        }
    }

    @Test

    fun `pass with game not started yet fails`() {
        val game = Game()
        assertFailsWith<InvalidGameException> {
            game.pass()
        }
    }

    @Test
    fun `pass with players empty fails`() {
        val uut = Game(
            target = false,
            gameState = GameState(
                players = emptyList(),
                lastPlayer = PieceType.BLACK,
                board = Board(4).startPieces()
            ),
            currGameName = null,
        )

        assertFailsWith<InvalidGameException> {
            uut.pass()
        }
    }

    @Test
    fun `pass no local game with 2 players succeeds`() {
        cleanup {
            val uut = startNewGame(
                players = listOf(
                    Player(PieceType.BLACK),
                ),
                currGameName = "testGame",
                firstTurn = PieceType.BLACK,
            ).copy(
                gameState = GameState(
                    players = listOf(
                        Player(PieceType.BLACK)
                    ),
                    lastPlayer = PieceType.WHITE,
                    board = Board(4)
                        .addPiece(Piece(Coordinate(1, 1), PieceType.BLACK))
                        .addPiece(Piece(Coordinate(1, 2), PieceType.BLACK))
                ),
                target = false,
            )

            uut.copy(
                gameState = uut.gameState?.copy(
                    players = listOf(uut.gameState.players[0].swap())
                )
            ).saveGame()

            var uut2 = loadGame("testGame")

            uut.pass()
            uut2 = uut2.refresh()

            assertFailsWith<EndGameException> {
                uut2.pass()
            }
        }
    }

    @Test
    fun `startNewGame and load game with 1 player succeeds`() {
        cleanup {
            val expectedPlayers = emptyList<Player>()
            val expectedBoard = Board(4).startPieces()
            val expectedLastPlayer = PieceType.WHITE

            startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "existingGame",
            )

            loadGame(
                gameName = "existingGame"
            )

            val loadedGame = STORAGE.load("existingGame")?.let {
                Game(
                    target = false,
                    gameState = it,
                    currGameName = "existingGame",
                )
            }!!

            assertEquals(expectedBoard, loadedGame.gameState?.board)
            assertEquals(expectedPlayers, loadedGame.gameState?.players)
            assertEquals(expectedLastPlayer, loadedGame.gameState?.lastPlayer)
        }
    }

    @Test
    fun `startNewGame in local game with 2 players succeeds`() {
        val uut = startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK), Player(PieceType.WHITE)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        val expectedBoard = Board(4).startPieces()
        val expectedPlayers = listOf(Player(PieceType.BLACK, 2), Player(PieceType.WHITE, 2))
        val expectedLastPlayer = PieceType.WHITE

        assertEquals(expectedBoard, uut.gameState?.board)
        assertEquals(expectedPlayers, uut.gameState?.players)
        assertEquals(expectedLastPlayer, uut.gameState?.lastPlayer)
    }

    @Test
    fun `startNewGame with empty players fails`() {
        assertFailsWith<InvalidGameException> {
            startNewGame(
                side = 4,
                players = emptyList(),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )
        }
    }

    @Test
    fun `startNewGame in local game with 1 player succeeds`() {
        val uut = startNewGame(
            side = 4,
            players = listOf(Player(PieceType.BLACK)),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        val expectedBoard = Board(4).startPieces()
        val expectedPlayers = listOf(Player(PieceType.BLACK, 2))
        val expectedLastPlayer = PieceType.WHITE

        assertEquals(expectedBoard, uut.gameState?.board)
        assertEquals(expectedPlayers, uut.gameState?.players)
        assertEquals(expectedLastPlayer, uut.gameState?.lastPlayer)
    }

    @Test
    fun `loadGame with non-existing game fails`() {
        cleanup {
            assertFailsWith<InvalidFileException> {
                loadGame(
                    gameName = "nonExistingGame"
                )
            }
        }
    }

    @Test
//      1 2 3 4
//    1 . . . .
//    2 . W B .
//    3 . B W .
//    4 . . . .
    fun `play with 1 player in not local game and not his turn fails`() {
        cleanup {
            startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            val uut = loadGame("testGame")

            assertFailsWith<InvalidPlayException> {
                uut.play(Coordinate(1, 2))
            }
        }
    }

    @Test
//      1 2 3 4
//    1 . . . .
//    2 . W B .
//    3 . B W .
//    4 . . . .
    fun `play with 1 player loading from not local game and is your turn succeeds`() {
        cleanup {
            startNewGame(
                side = 4,
                players = listOf(Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            val uut = loadGame("testGame")

            val expectedBoard = Board(4).startPieces()
                .addPiece(Coordinate(1, 2), PieceType.BLACK)
                .changePiece(Coordinate(2, 2))

            val resultGame = uut.play(Coordinate(1, 2))

            assert(expectedBoard == resultGame.gameState?.board)
        }
    }

    @Test
//      1 2 3 4
//    1 . . . .
//    2 . W B .
//    3 . B W .
//    4 . . . .
    fun `play with 1 player creating from not local game and is your turn succeeds`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            val expectedBoard = Board(4).startPieces()
                .addPiece(Coordinate(1, 2), PieceType.BLACK)
                .changePiece(Coordinate(2, 2))

            val resultGame = uut.play(Coordinate(1, 2))

            assert(expectedBoard == resultGame.gameState?.board)
        }
    }

    @Test
            /*
          1 2 3 4
        1 . . . .
        2 . W W W
        3 . B B B
        4 . . . .
               */
    fun `play in not local game verify players point update correctly and playerTurn after normal plays`() {
        cleanup {
            val expectedBlackPoints = 3
            val expectedWhitePoints = 3
            val expectedPlayerTurn = PieceType.WHITE

            var uutB = startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            var uutW = loadGame("testGame")

            uutB = uutB.play(Coordinate(3, 4))

            uutW = uutW.refresh()
            uutW = uutW.play(Coordinate(2, 4))

            uutB = uutB.refresh()
            uutW = uutW.refresh()

            assertEquals(expectedBlackPoints, uutB.gameState?.players[0]?.points)
            assertEquals(expectedWhitePoints, uutW.gameState?.players[0]?.points)
            assertEquals(expectedPlayerTurn, uutB.gameState?.lastPlayer)
        }
    }

    @Test
    fun `pass in local game with available plays fails`() {
        val uut = startNewGame(
            side = 4,
            players = listOf(
                Player(PieceType.BLACK),
                Player(PieceType.WHITE)
            ),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        assertFailsWith<InvalidPlayException> {
            uut.pass()
        }

        assertEquals(PieceType.WHITE, uut.gameState?.lastPlayer)
    }

    @Test
    fun `pass in not local game with available plays fails`() {
        cleanup {
            var uutB = startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            var uutW = loadGame("testGame")

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
    fun `refresh in local game returns same game`() {
        val uut = startNewGame(
            side = 4,
            players = listOf(
                Player(PieceType.BLACK),
                Player(PieceType.WHITE)
            ),
            firstTurn = PieceType.BLACK,
            currGameName = null,
        )

        val refreshedGame = uut.refresh()

        assertEquals(uut, refreshedGame)
    }

    @Test
    fun `refresh in not local game loads updated game state`() {
        cleanup {
            var uutB = startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            var uutW = loadGame("testGame")

            uutB = uutB.play(Coordinate(1, 2))

            uutW = uutW.refresh()

            assertEquals(uutB.gameState?.board, uutW.gameState?.board)
            assertEquals(uutB.gameState?.lastPlayer, uutW.gameState?.lastPlayer)
        }
    }

    @Test
    fun `saveGame with game not started yet fails`() {
        val game = Game()

        assertFailsWith<InvalidGameException> {
            game.saveGame()
        }
    }

    @Test
    fun `saveGame with players empty fails`() {
        val uut = Game(
            target = false,
            gameState = GameState(
                players = emptyList(),
                lastPlayer = PieceType.BLACK,
                board = Board(4).startPieces()
            ),
            currGameName = "testGame",
        )

        assertFailsWith<InvalidGameException> {
            uut.saveGame()
        }
    }

    @Test
    fun `saveGame in not local game succeeds`() {
        cleanup {
            val uut = newGameForTest(
                board = Board(4),
                players = listOf(Player(PieceType.BLACK),Player(PieceType.WHITE)),
                lastPlayer = PieceType.WHITE,
                currGameName = "testGame",
            )

            uut.saveGame()

            val loadedGameState = STORAGE.load("testGame")

            val expectedGameState = uut.gameState?.copy(
                players = listOf(Player(PieceType.BLACK),Player(PieceType.WHITE)),
            )

            assertEquals(expectedGameState, loadedGameState)
        }
    }

    @Test
    fun `saveGame in local game succeeds`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = listOf(Player(PieceType.BLACK),Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            uut.saveGame()

            val loadedGameState = STORAGE.load("testGame")

            assertEquals(uut.gameState, loadedGameState)
        }
    }
}