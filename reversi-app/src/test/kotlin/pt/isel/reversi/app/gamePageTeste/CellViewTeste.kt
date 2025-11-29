package pt.isel.reversi.app.gamePageTeste

import androidx.compose.ui.test.*
import pt.isel.reversi.app.gamePage.cellView
import pt.isel.reversi.app.gamePage.getCellViewTestTag
import pt.isel.reversi.app.gamePage.getPieceTestTag
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType
import kotlin.test.Test
import kotlin.test.fail

/**
 * Tests for the CellView composable function.
 * Have 3 possible states:
 * 1. No piece (cellValue and ghostPiece are null) -> no piece view and clickable
 * 2. Piece present (cellValue is not null) -> piece view and not clickable
 * 3. Ghost piece present (ghostPiece is not null and cellValue is null) -> piece view and clickable
 * If both cellValue and ghostPiece are not null, cellValue takes precedence.
 */
@OptIn(ExperimentalTestApi::class)
class CellViewTeste {
    @Test
    fun `cellView with cellValue null and ghostPiece null expect no piece view`() = runComposeUiTest {
        val expectedCoordinates = Coordinate(1, 1)

        setContent {
            cellView(
                coordinate = expectedCoordinates,
                cellValue = null,
                ghostPiece = null,
                onClick = { fail("onClick should not be called") }
            )
        }
        val cellViewTag = getCellViewTestTag(coordinate = expectedCoordinates)
        onNodeWithTag(testTag = cellViewTag, useUnmergedTree = true)
            .onChildren()[0]
            .assertDoesNotExist()
    }

    @Test
    fun `cellView with cellValue != null expect existing piece view and not clickable (Piece)`() = runComposeUiTest {
        val expectedCoordinates = Coordinate(1, 1)
        val expectedValue = PieceType.BLACK
        setContent {
            cellView(
                coordinate = expectedCoordinates,
                cellValue = expectedValue,
                ghostPiece = null,
                onClick = { fail("onClick should not be called") }
            )
        }

        val pieceTag = getPieceTestTag(coordinate = expectedCoordinates, type = expectedValue)
        onNodeWithTag(testTag = pieceTag, useUnmergedTree = true)
            .assertExists()

        val cellViewTag = getCellViewTestTag(coordinate = expectedCoordinates)
        onNodeWithTag(testTag = cellViewTag, useUnmergedTree = true)
            .assertIsNotEnabled()
    }

    @Test
    fun `cellView with ghostPiece != null and pieceType null expect existing piece and clickable (ghostPiece)`() =
        runComposeUiTest {
            val expectedCoordinates = Coordinate(1, 1)
            val expectedValue = PieceType.BLACK
            setContent {
                cellView(
                    coordinate = expectedCoordinates,
                    cellValue = null,
                    ghostPiece = expectedValue,
                    onClick = { fail("onClick should not be called") }
                )
            }
            val pieceTag = getPieceTestTag(coordinate = expectedCoordinates, type = expectedValue)
            onNodeWithTag(testTag = pieceTag, useUnmergedTree = true)
                .assertExists()

            val cellViewTag = getCellViewTestTag(coordinate = expectedCoordinates)
            onNodeWithTag(testTag = cellViewTag, useUnmergedTree = true)
                .assertIsEnabled()
        }

    @Test
    fun `cellView with both cellValue and ghostPiece not null expect existing piece and not clickable (Piece)`() =
        runComposeUiTest {
            val expectedCoordinates = Coordinate(1, 1)
            val cellValue = PieceType.WHITE
            val ghostPiece = PieceType.BLACK
            setContent {
                cellView(
                    coordinate = expectedCoordinates,
                    cellValue = cellValue,
                    ghostPiece = ghostPiece,
                    onClick = { fail("onClick should not be called") }
                )
            }
            val pieceTag = getPieceTestTag(coordinate = expectedCoordinates, type = cellValue)
            onNodeWithTag(testTag = pieceTag, useUnmergedTree = true)
                .assertExists()

            val cellViewTag = getCellViewTestTag(coordinate = expectedCoordinates)
            onNodeWithTag(testTag = cellViewTag, useUnmergedTree = true)
                .assertIsNotEnabled()
        }

    @Test
    fun `cellView with empty cell and freeze true expect not clickable`() = runComposeUiTest {
        val expectedCoordinates = Coordinate(1, 1)
        setContent {
            cellView(
                coordinate = expectedCoordinates,
                cellValue = null,
                ghostPiece = null,
                freeze = true,
                onClick = { fail("onClick should not be called") }
            )
        }

        val cellViewTag = getCellViewTestTag(coordinate = expectedCoordinates)
        onNodeWithTag(testTag = cellViewTag, useUnmergedTree = true)
            .assertIsNotEnabled()
    }

    @Test
    fun `cellView with ghostPiece and freeze true expect not clickable`() = runComposeUiTest {
        val expectedCoordinates = Coordinate(1, 1)
        val expectedValue = PieceType.BLACK
        setContent {
            cellView(
                coordinate = expectedCoordinates,
                cellValue = null,
                ghostPiece = expectedValue,
                freeze = true,
                onClick = { fail("onClick should not be called") }
            )
        }

        val pieceTag = getPieceTestTag(coordinate = expectedCoordinates, type = expectedValue)
        onNodeWithTag(testTag = pieceTag, useUnmergedTree = true)
            .assertExists()

        val cellViewTag = getCellViewTestTag(coordinate = expectedCoordinates)
        onNodeWithTag(testTag = cellViewTag, useUnmergedTree = true)
            .assertIsNotEnabled()
    }
}