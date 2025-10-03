package pt.isel.reversi.game

import pt.isel.reversi.board.Piece
import pt.isel.reversi.board.PieceType
import pt.isel.reversi.board.Board

interface GameDataAccessImpl {
    // POST METHODS
    fun postPiece(piece: Piece, name: String)
    fun postGame(game: GameImpl, name: String)
    fun postInitGame(side: Int, name: String)
    fun postAvailablePiece(piece: List<PieceType>, name: String)

    // GET METHODS
    fun getBoard(name: String): Board
    fun getAvailablePieces(name: String): List<PieceType>
    fun getLatestPiece(name: String): Piece?

    // TODO ESTRUTURA FICHEIRO PARA IMPL COM FILES

    // availablePieces: @|#
    // side: n
    // piece: row col type
    // piece: row col type
    // piece: row col type
    // pass: type
}