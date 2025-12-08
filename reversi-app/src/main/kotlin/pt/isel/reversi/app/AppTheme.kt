package pt.isel.reversi.app

import androidx.compose.ui.graphics.Color

data class AppTheme(
    val backgroundMusic: String = "background-music",
    val gameMusic: String = "MEGALOVANIA",
    val placePieceSound: String = "putPiece",
    val textColor: Color = Color(0xFFFFFFFF),
    val backGroundColor: Color = Color(0xFF121212),
    val primaryColor: Color = Color(0xFF1976D2),
    val secondaryColor: Color = Color(0xFF1E1E1E),
    val boardColor: Color = Color(0xFF009000)
)