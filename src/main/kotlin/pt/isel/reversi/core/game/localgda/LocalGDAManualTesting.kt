package pt.isel.reversi.core.game.localgda

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.game.GameImpl
import pt.isel.reversi.core.game.MockGame
import pt.isel.reversi.core.game.Player
import pt.isel.reversi.core.game.data.GDAResult
import java.io.File
import java.nio.file.Files

fun main() {
    val dataAccess = LocalGDA()
    Files.createTempDirectory("reversi-test-")
    // val path = dir.resolve("game${(0..100).random()}.txt").toString()
    val path = "game.txt"
    println("Using file: $path")
    var game: MockGame = MockGame.OnePlayer(dataAccess, path)
    val game2 = MockGame.OnePlayer(dataAccess, path)

    val resultsList = mutableListOf<GDAResult<*>>()

    println("Press Enter to start tests...")
    readln()
    resultsList += dataAccess.postGame(path, game)
    File(path).forEachLine { println(it) }
    println("Press Enter to continue...")
    readln()
    resultsList += dataAccess.postGame(path, game2)
    File(path).forEachLine { println(it) }
    println("Press Enter to continue...")
    readln()
    resultsList += dataAccess.postPiece(path, Piece(Coordinate(2, 3), PieceType.BLACK))

    val result = dataAccess.getBoard(path)
    resultsList += result
    if (result.data != null)
        game = game.copy(dataAccess, board = result.data)

    readln()
    resultsList += dataAccess.postGame(path, game)

    resultsList.forEach { println(it.toStringColored()) }
    readln()
}