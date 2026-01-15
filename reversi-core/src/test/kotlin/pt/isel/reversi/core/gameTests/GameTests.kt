package pt.isel.reversi.core.gameTests

import pt.isel.reversi.core.Game
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.cleanup
import pt.isel.reversi.core.exceptions.InvalidFileException
import pt.isel.reversi.core.exceptions.InvalidGameException
import pt.isel.reversi.core.exceptions.InvalidPlayException
import pt.isel.reversi.core.gameServices.EmptyGameService
import pt.isel.reversi.core.startNewGame
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.MatchPlayers
import pt.isel.reversi.utils.LOGGER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class GameTests {

    @Test
    fun `play with game not started yet`() {
        cleanup {
            val game = Game(
                service = EmptyGameService()
            )

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
                service = EmptyGameService()
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
                service = EmptyGameService()
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
                service = EmptyGameService()
            )

            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
                service = EmptyGameService()
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
                service = EmptyGameService()
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
                service = EmptyGameService()
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
                myPiece = PieceType.BLACK,
                service = EmptyGameService()
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
            val game = Game(
                service = EmptyGameService()
            )
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
                service = EmptyGameService()
            )

            assertFailsWith<InvalidGameException> {
                uut.pass()
            }
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
                service = EmptyGameService()
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
                    service = EmptyGameService()
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
                service = EmptyGameService()
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
                service = EmptyGameService()
            )

            assertFailsWith<InvalidPlayException> {
                uut.pass()
            }

            assertEquals(PieceType.WHITE, uut.gameState?.lastPlayer)
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
                service = EmptyGameService()
            )

            val refreshedGame = uut.refresh()

            assertEquals(uut, refreshedGame)
        }
    }


    @Test
    fun `saveOnlyBoard in local game fails`() {
        cleanup {
            val uut = startNewGame(
                side = 4,
                players = MatchPlayers(Player(PieceType.BLACK), Player(PieceType.WHITE)),
                firstTurn = PieceType.BLACK,
                currGameName = null,
                service = EmptyGameService()
            )

            assertFailsWith<InvalidFileException> {
                uut.saveOnlyBoard(uut.gameState)
            }
        }
    }

}