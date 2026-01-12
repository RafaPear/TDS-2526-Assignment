package pt.isel.reversi.core

import kotlinx.coroutines.runBlocking
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.InvalidFileException
import pt.isel.reversi.core.exceptions.InvalidGameException
import pt.isel.reversi.core.exceptions.InvalidNameAlreadyExists
import pt.isel.reversi.core.exceptions.InvalidPlayException
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.MatchPlayers
import pt.isel.reversi.utils.LOGGER
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class GameTests {

    fun cleanup(func: suspend () -> Unit) {
        val conf = loadCoreConfig()
        File(conf.savesPath).deleteRecursively()
        runBlocking { func() }
        File(conf.savesPath).deleteRecursively()
    }

    @Test
    fun `startNewGame with already existing name in storage fails`() {
        cleanup {
            startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "existingGame",
            )

            assertFailsWith<InvalidNameAlreadyExists> {
                startNewGame(
                    side = 4,
                    players = MatchPlayers(Player(PieceType.WHITE)),
                    firstTurn = PieceType.WHITE,
                    currGameName = "existingGame",
                )
            }
        }
    }

    @Test
    fun `play with game not started yet`() {
        cleanup {
            val game = Game()

            assertFailsWith<InvalidGameException> {
                game.play(Coordinate(1, 1))
            }
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
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )

            assertFailsWith<InvalidPlayException> {
                uut.play(Coordinate(1, 2))
            }
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
    fun `play with 1 player in local game fails`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )

            assertFails { (uut.play(Coordinate(1, 2))) }
        }
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
        cleanup {
            val expectedBoard = Board(4).startPieces()
                .addPiece(Coordinate(1, 2), PieceType.BLACK)
                .changePiece(Coordinate(2, 2))

            val expectedGame = Game(
                gameState = GameState(
                    lastPlayer = PieceType.BLACK,
                    board = expectedBoard,
                    players = MatchPlayers(
                        Player(PieceType.BLACK, points = 4),
                        Player(PieceType.WHITE, points = 1)
                    )
                ),
                target = false,
                currGameName = null,
            )

            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            ).play(Coordinate(1, 2))

            LOGGER.info { "Expected Game State:\n${expectedGame.gameState}\nUUT Game State:\n${uut.gameState}" }

            assert(expectedGame.gameState == uut.gameState)
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
    fun `play in local game verify players point update correctly and playerTurn after normal plays`() {
        cleanup {
            val expectedBlackPoints = 3
            val expectedWhitePoints = 3
            val expectedPlayerTurn = PieceType.WHITE

            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )
                .play(Coordinate(3, 4))
                .play(Coordinate(2, 4))

            assertEquals(expectedBlackPoints, uut.gameState?.players?.find { it.type == PieceType.BLACK }!!.points)
            assertEquals(expectedWhitePoints, uut.gameState.players.find { it.type == PieceType.WHITE }!!.points)
            assertEquals(expectedPlayerTurn, uut.gameState.lastPlayer)
        }
    }

    @Test
    fun `play with players empty fails`() {
        cleanup {
            val uut = Game(
                target = false,
                gameState = GameState(
                    players = MatchPlayers(),
                    lastPlayer = PieceType.BLACK,
                    board = Board(4).startPieces(),
                ),
                currGameName = null,
            )

            assertFailsWith<InvalidGameException> {
                uut.play(Coordinate(1, 2))
            }
        }
    }

    @Test
    fun `pass in localGame when no availablePlays succeeds`() {
        cleanup {
            var uut = Game(
                target = false,
                gameState = GameState(
                    players = MatchPlayers(
                        Player(PieceType.BLACK, "Player 1"),
                        Player(PieceType.WHITE, "Player 2")
                    ),
                    board = Board(4)
                        .addPiece(Piece(Coordinate(1, 1), PieceType.BLACK))
                        .addPiece(Piece(Coordinate(1, 2), PieceType.BLACK)),
                    lastPlayer = PieceType.BLACK,
                ),
                currGameName = null,
                myPiece = PieceType.BLACK
            ).pass()

            val expectedCountPass = 1
            val expectedPlayerTurn = PieceType.WHITE
            assertEquals(expectedCountPass, uut.countPass)
            assertEquals(expectedPlayerTurn, uut.gameState?.lastPlayer)


            uut = uut.pass()


            assertEquals(Player(PieceType.BLACK, points = 2), uut.gameState?.winner)
        }
    }

    @Test

    fun `pass with game not started yet fails`() {
        cleanup {
            val game = Game()
            assertFailsWith<InvalidGameException> {
                game.pass()
            }
        }
    }

    @Test
    fun `pass with players empty fails`() {
        cleanup {
            val uut = Game(
                target = false,
                gameState = GameState(
                    players = MatchPlayers(),
                    lastPlayer = PieceType.BLACK,
                    board = Board(4).startPieces()
                ),
                currGameName = null,
            )

            assertFailsWith<InvalidGameException> {
                uut.pass()
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
            )
            val gameState = uut.gameState!!.copy(
                board = Board(4)
                    .addPiece(Piece(Coordinate(1, 1), PieceType.BLACK))
                    .addPiece(Piece(Coordinate(1, 2), PieceType.BLACK))
            )
            uut.saveOnlyBoard(gameState)
            uut = uut.refresh()

            var uut2 = loadGame(
                gameName = uut.currGameName!!,
                desiredType = null
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
            )


            val loadedGame = game.storage.load("existingGame")?.let {
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
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )

            val expectedBoard = Board(4).startPieces()
            val expectedPlayers = MatchPlayers(Player(PieceType.BLACK, points = 2), Player(PieceType.WHITE, points = 2))
            val expectedLastPlayer = PieceType.WHITE

            assertEquals(expectedBoard, uut.gameState?.board)
            assertEquals(expectedPlayers, uut.gameState?.players)
            assertEquals(expectedLastPlayer, uut.gameState?.lastPlayer)
        }
    }

    @Test
    fun `startNewGame with empty players fails`() {
        cleanup {
            assertFailsWith<InvalidGameException> {
                startNewGame(
                    side = 4,
                    players = MatchPlayers(),
                    firstTurn = PieceType.BLACK,
                    currGameName = null,
                )
            }
        }
    }

    @Test
    fun `startNewGame in local game with 1 player succeeds`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )

            val expectedBoard = Board(4).startPieces()
            val expectedPlayers = MatchPlayers(Player(PieceType.BLACK, points = 2))
            val expectedLastPlayer = PieceType.WHITE

            assertEquals(expectedBoard, uut.gameState?.board)
            assertEquals(expectedPlayers, uut.gameState?.players)
            assertEquals(expectedLastPlayer, uut.gameState?.lastPlayer)
        }
    }

    @Test
    fun `loadGame with non-existing game fails`() {
        cleanup {
            assertFailsWith<InvalidFileException> {
                loadGame(
                    gameName = "nonExistingGame",
                    desiredType = null
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
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            val uut = loadGame(
                gameName = "testGame",
                desiredType = null
            )

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
                players = MatchPlayers(Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            val uut = loadGame(
                gameName = "testGame",
                desiredType = null
            )

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
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            loadGame(
                gameName = "testGame",
                desiredType = null
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
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            var uutW = loadGame(
                gameName = "testGame",
                desiredType = null
            )

            uutB = uutB.play(Coordinate(3, 4))

            uutW = uutW.refresh()
            uutW = uutW.play(Coordinate(2, 4))

            uutB = uutB.refresh()
            uutW = uutW.refresh()

            assertEquals(expectedBlackPoints, uutB.gameState?.players?.getFirstPlayer()?.points)
            assertEquals(expectedWhitePoints, uutW.gameState?.players?.getFirstPlayer()?.points)
            assertEquals(expectedPlayerTurn, uutB.gameState?.lastPlayer)
        }
    }

    @Test
    fun `pass in local game with available plays fails`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(
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
    }

    @Test
    fun `pass in not local game with available plays fails`() {
        cleanup {
            var uutB = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            var uutW = loadGame(
                gameName = "testGame",
                desiredType = null
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
    fun `refresh in local game returns same game`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(
                    Player(PieceType.BLACK),
                    Player(PieceType.WHITE)
                ),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )

            val refreshedGame = uut.refresh()

            assertEquals(uut, refreshedGame)
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
            )

            var uutW = loadGame(
                gameName = "testGame",
                desiredType = null
            )

            uutW = uutW.play(uutW.getAvailablePlays().first())

            uutB = uutB.refresh()
            assertEquals(uutW.gameState, uutB.gameState)
        }
    }

    @Test
    fun `saveEndGame with game not started yet fails`() {
        cleanup {
            val game = Game()

            assertFailsWith<InvalidGameException> {
                game.saveEndGame()
            }
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
            )

            assertFailsWith<InvalidGameException> {
                uut.saveEndGame()
            }
        }
    }

    @Test
    fun `saveEndGame in not local game succeeds`() {
        cleanup {
            val uut = startNewGame(
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.WHITE,
                currGameName = "testGame",
                side = 4
            )

            uut.saveEndGame()

            val loadedGameState = uut.storage.load("testGame")

            val expectedGameState = uut.gameState?.copy(
                players = MatchPlayers(Player(PieceType.BLACK)).refreshPlayers(uut.gameState.board),
            )

            assertEquals(expectedGameState, loadedGameState)
        }
    }

    @Test
    fun `saveEndGame in local game succeeds`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            val expectedGameState = uut.gameState?.copy(
                players = MatchPlayers(uut.gameState.players.getPlayerByType(PieceType.WHITE)),
            )
            uut.saveEndGame()

            val loadedGameState = uut.storage.load("testGame")

            assertEquals(expectedGameState, loadedGameState)
        }
    }

    @Test
    fun `saveOnlyBoard in local game and name != null succeeds`() {
        cleanup {
            var uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = "testGame",
            )

            uut = uut.play(Coordinate(1, 2))

            uut.saveOnlyBoard(uut.gameState)

            val loadedGameState = uut.storage.load("testGame")

            assertEquals(uut.gameState?.board, loadedGameState?.board)
            assert(loadedGameState?.players?.isFull()!!)
            assertEquals(uut.gameState?.lastPlayer, loadedGameState.lastPlayer)
        }
    }

    @Test
    fun `saveOnlyBoard in local game and name == null fails`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
            )

            assertFailsWith<InvalidFileException> {
                uut.saveOnlyBoard(uut.gameState)
            }
        }
    }

    @Test
    fun `GameState changeName updates the correct player's name`() {
        val player1 = Player(type = PieceType.BLACK, name = "Alice")
        val player2 = Player(type = PieceType.WHITE, name = "Bob")
        val expectedPlayer1 = player1.copy(name = "Charlie")
        val expectedPlayer2 = player2.copy(name = "Diana")

        val initialPlayers = MatchPlayers(
            player1 = player1,
            player2 = player2
        )


        val gameState = GameState(
            players = initialPlayers,
            lastPlayer = PieceType.BLACK,
            board = Board(4).startPieces()
        )

        val updatedGameStateBlack = gameState.changeName(
            newName = expectedPlayer1.name,
            pieceType = expectedPlayer1.type
        )

        assertEquals(expectedPlayer1, updatedGameStateBlack.players.player1)
        assertEquals(player2, updatedGameStateBlack.players.player2)

        val updatedGameStateWhite = gameState.changeName(
            newName = expectedPlayer2.name,
            pieceType = expectedPlayer2.type
        )

        assertEquals(expectedPlayer2, updatedGameStateWhite.players.player2)
        assertEquals(player1, updatedGameStateWhite.players.player1)
    }

    @Test
    fun `GameState changeName with non-existing piece type does not change players`() {
        val player1 = Player(type = PieceType.BLACK, name = "Alice")

        val initialPlayers = MatchPlayers(
            player1 = player1,
        )

        val gameState = GameState(
            players = initialPlayers,
            lastPlayer = PieceType.BLACK,
            board = Board(4).startPieces()
        )

        val updatedGameState = gameState.changeName(
            newName = "Charlie",
            pieceType = PieceType.WHITE // Non-existing piece type in this context
        )

        assertEquals(initialPlayers, updatedGameState.players)
    }

    @Test
    fun `GameState refreshPlayers updates players based on current board state`() {
        val player1 = Player(type = PieceType.BLACK, name = "Alice", points = 0)
        val player2 = Player(type = PieceType.WHITE, name = "Bob", points = 0)

        val initialPlayers = MatchPlayers(
            player1 = player1,
            player2 = player2
        )

        val board = Board(4)
            .addPiece(Coordinate(1, 1), PieceType.BLACK)
            .addPiece(Coordinate(1, 2), PieceType.BLACK)
            .addPiece(Coordinate(2, 1), PieceType.WHITE)

        val gameState = GameState(
            players = initialPlayers,
            lastPlayer = PieceType.BLACK,
            board = board
        )

        val refreshedGameState = gameState.refreshPlayers()

        val expectedPlayer1Points = 2 // BLACK pieces
        val expectedPlayer2Points = 1 // WHITE pieces

        assertEquals(expectedPlayer1Points, refreshedGameState.players.player1?.points)
        assertEquals(expectedPlayer2Points, refreshedGameState.players.player2?.points)
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

            val lsGameState = uut.storage.load(uut.currGameName!!)

            assertEquals(expectedMyPlayer, lsGameState?.players?.getPlayerByType(expectedMyPlayer.type))
            assertEquals(initialPlayer2, lsGameState?.players?.getPlayerByType(initialPlayer2.type))
        }
    }
}