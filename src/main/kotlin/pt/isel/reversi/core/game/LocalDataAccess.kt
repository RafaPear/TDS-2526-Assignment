package pt.isel.reversi.core.game

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Piece
import pt.isel.reversi.core.board.PieceType

class LocalDataAccess: GameDataAccessImpl {
    override fun postPiece(piece: Piece, name: String) {
        TODO("Not yet implemented")
    }

    override fun postGame(game: GameImpl, name: String) {
        TODO("Not yet implemented")
    }

    override fun postInitGame(side: Int, name: String) {
        TODO("Not yet implemented")
    }

    override fun postAvailablePiece(
        piece: List<PieceType>,
        name: String
    ) {
        TODO("Not yet implemented")
    }

    override fun getBoard(name: String): Board {
        TODO("Not yet implemented")
    }

    override fun getAvailablePieces(name: String): List<PieceType> {
        TODO("Not yet implemented")
    }

    override fun getLatestPiece(name: String): Piece? {
        TODO("Not yet implemented")
    }
}