package pt.isel.reversi.app

import androidx.compose.ui.graphics.Color


data class AppTheme(
    val name: String,
    val backgroundMusic: String = "background-music",
    val gameMusic: String = "MEGALOVANIA",
    val placePieceSound: String = "putPiece",
    val textColor: Color = Color(0xFF000000),
    val buttonTextColor: Color = Color(0xFFFFFFFF),
    val backgroundColor: Color = Color(0xFFFFFFFF),
    val primaryColor: Color = Color(0xFF1976D2),
    val secondaryColor: Color = Color(0xFFFE4E4E),
    val boardColor: Color = Color(0xFF009000),
    val boardBgColor: Color = Color(0xFF006400),
    val boardSideColor: Color = Color(0xFF000000),
    val darkPieceColor: Color = Color(0xFF000000),
    val lightPieceColor: Color = Color(0xFFFFFFFF),
)