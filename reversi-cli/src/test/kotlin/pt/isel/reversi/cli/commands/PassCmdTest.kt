package pt.isel.reversi.cli.commands

import pt.isel.reversi.cli.cleanup
import pt.isel.reversi.core.Player
import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.newGameForTest
import pt.rafap.ktflag.cmd.CommandResultType
import kotlin.test.Test

class PassCmdTest {

    @Test
    fun `Test PassCmd execution`() {
        cleanup {
            val result = PassCmd.executeWrapper(
                context = newGameForTest(
                    players = listOf(
                        Player(PieceType.BLACK),
                    ),
                    board = Board(4)
                        .addPiece(Piece(Coordinate(1, 1), PieceType.BLACK))
                        .addPiece(Piece(Coordinate(1, 2), PieceType.BLACK)),
                    lastPlayer = PieceType.WHITE
                )
            )
            assert(result.type == CommandResultType.SUCCESS) {
                "Expected SUCCESS but got ${result.type} with message: ${result.message}"
            }
        }
    }

    @Test
    fun `Test PassCmd execution fails on null game`() {
        cleanup {
            val result = PassCmd.executeWrapper(context = null)
            assert(result.type == CommandResultType.ERROR) {
                "Expected ERROR but got ${result.type} with message: ${result.message}"
            }
        }
    }

    @Test
    fun `Test PassCmd fails execution by arguments`() {
        cleanup {
            val args = arrayOf("extraArg")
            val result = PassCmd.executeWrapper(*args, context = null)
            assert(result.type == CommandResultType.INVALID_ARGS) {
                "Expected INVALID_ARGS but got ${result.type} with message: ${result.message}"
            }
        }
    }
}