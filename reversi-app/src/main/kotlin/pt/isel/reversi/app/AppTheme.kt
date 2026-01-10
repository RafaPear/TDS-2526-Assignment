package pt.isel.reversi.app

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

/**
 * Data class representing a theme configuration for the Reversi application.
 * Contains colors for UI elements and audio resources for different game states.
 *
 * @property name The name of the theme.
 * @property backgroundMusic Resource name for background music.
 * @property gameMusic Resource name for gameplay music.
 * @property placePieceSound Resource name for the piece placement sound effect.
 * @property textColor Color used for text elements.
 * @property buttonTextColor Color used for button text.
 * @property backgroundColor Primary background color.
 * @property primaryColor Primary accent color.
 * @property secondaryColor Secondary accent color.
 * @property boardColor Color of the game board.
 * @property boardBgColor Background color of the game board.
 * @property boardSideColor Color of the board's sides/borders.
 * @property darkPieceColor Color for dark-colored pieces.
 * @property lightPieceColor Color for light-colored pieces.
 */
data class AppTheme(
    val name: String,
    val backgroundMusic: String = "background-music",
    val gameMusic: String = "MEGALOVANIA",
    val placePieceSound: String = "putPiece",
    val textColor: Color = Color(0xFF000000),
    // Image resource for background image
    val backgroundImage: DrawableResource? = null,
    val backgroundColor: Color = Color(0xFFFFFFFF),
    val buttonTextColor: Color = Color(0xFFFFFFFF),
    val primaryColor: Color = Color(0xFF1976D2),
    val secondaryColor: Color = Color(0xFFFE4E4E),
    val boardColor: Color = Color(0xFF009000),
    val boardBgColor: Color = Color(0xFF006400),
    val boardSideColor: Color = Color(0xFF000000),
    val darkPieceColor: Color = Color(0xFF000000),
    val lightPieceColor: Color = Color(0xFFFFFFFF),
) {
    fun getAudioNames(): List<String> =
        listOf(backgroundMusic, gameMusic, placePieceSound)

    fun getAudioMusicNames(): List<String> =
        listOf(backgroundMusic, gameMusic)
}