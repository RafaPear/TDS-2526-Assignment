package pt.isel.reversi.board

import kotlin.test.assertFailsWith

//class testsIncomplete {
//    @Test
//    fun `Last Piece added is at the end of the Piece list succeeds`() {
//        val uut = Board(4)
//            .addPiece(Coordinates(1, 'a'), PieceType.WHITE)
//            .addPiece(Coordinates(3, 'c'), PieceType.BLACK)
//        val lastPiece = uut.last()
//        assert(Coordinates(3, 'c') == lastPiece.coordinate)
//        assert(PieceType.BLACK == lastPiece.value)
//    }
//
//    @Test
//    fun `Last Piece added is at the end of the Piece list fails`() {
//        val uut = Board(4)
//            .addPiece(Coordinates(1, 'a'), PieceType.WHITE)
//            .addPiece(Coordinates(3, 'c'), PieceType.BLACK)
//        val lastPiece = uut.last()
//        assertFailsWith<IllegalArgumentException> { Coordinates(1, 'a') == lastPiece.coordinate }
//        assertFailsWith<IllegalArgumentException> { PieceType.WHITE == lastPiece.value }
//    }
//}