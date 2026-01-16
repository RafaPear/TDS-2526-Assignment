package pt.isel.reversi.app.app

import androidx.compose.ui.graphics.Color
import reversi.reversi_app.generated.resources.Res
import reversi.reversi_app.generated.resources.benfica_background
import reversi.reversi_app.generated.resources.matrix_background
import reversi.reversi_app.generated.resources.polish_cow_background

private val darkTheme = AppTheme(
    name = "Dark Default",
    backgroundMusic = "WiiParty",
    gameMusic = "MEGALOVANIA",
    placePieceSound = "putPiece",
    textColor = Color(0xFFFFFFFF),
    buttonTextColor = Color(0xFFFFFFFF),
    backgroundColor = Color(0xFF121212),
    primaryColor = Color(0xFF1976D2),
    secondaryColor = Color(0xFF1E1E1E),
    boardColor = Color(0xFF009000),
    boardBgColor = Color(0xFF004D00),
    boardSideColor = Color(0xFFFFFFFF),
    darkPieceColor = Color(0xFF1A1A1A),
    lightPieceColor = Color(0xFFF5F5F5)
)

private val lightTheme = AppTheme(
    name = "Light Default",
    backgroundMusic = "WiiParty",
    gameMusic = "MEGALOVANIA",
    placePieceSound = "putPiece",
    textColor = Color(0xFF000000),
    buttonTextColor = Color(0xFFFFFFFF),
    backgroundColor = Color(0xFFFFFFFF),
    primaryColor = Color(0xFF1976D2),
    secondaryColor = Color(0xFFF5F5F5),
    boardColor = Color(0xFF009000),
    boardBgColor = Color(0xFF006400),
    boardSideColor = Color(0xFF000000),
    darkPieceColor = Color(0xFF212121),
    lightPieceColor = Color(0xFFFFFFFF)
)

private val matrixTheme = AppTheme(
    name = "Matrix",
    backgroundMusic = "matrix-background-music",
    gameMusic = "matrix-game-music",
    placePieceSound = "putPiece",
    textColor = Color(0xFF00FF00),
    buttonTextColor = Color(0xFF000000),
    backgroundImage = Res.drawable.matrix_background,
    backgroundColor = Color(0xFF000000),
    primaryColor = Color(0xFF00FF00),
    secondaryColor = Color(0xFF0A0A0A),
    boardColor = Color(0xFF003300),
    boardBgColor = Color(0xFF000000),
    boardSideColor = Color(0xFF00FF00),
    darkPieceColor = Color(0xFF001100),
    lightPieceColor = Color(0xFF00FF00)
)

private val cyberpunkTheme = AppTheme(
    name = "Cyberpunk",
    backgroundMusic = "cyberpunk-background",
    gameMusic = "cyberpunk-battle",
    placePieceSound = "putPiece",
    textColor = Color(0xFF00F0FF),
    buttonTextColor = Color(0xFF000000),
    backgroundColor = Color(0xFF0D0221),
    primaryColor = Color(0xFFFCEE0A),
    secondaryColor = Color(0xFF1A0933),
    boardColor = Color(0xFF2D1B69),
    boardBgColor = Color(0xFF0D0221),
    boardSideColor = Color(0xFFFF0099),
    darkPieceColor = Color(0xFFFCEE0A),
    lightPieceColor = Color(0xFFFF0099)
)

private val benficaTheme = AppTheme(
    name = "Benfica",
    backgroundMusic = "benfica-background-music",
    gameMusic = "benfica-game-music",
    placePieceSound = "benfica-putPiece",
    backgroundImage = Res.drawable.benfica_background,
    textColor = Color(0xFFFFFFFF),
    buttonTextColor = Color(0xFFFFFFFF),
    backgroundColor = Color(0xFF1A0000),
    primaryColor = Color(0xFFDC143C),
    secondaryColor = Color(0xFF2D0000),
    boardColor = Color(0xFF8B0000),
    boardBgColor = Color(0xFF1A0000),
    boardSideColor = Color(0xFFFF4444),
    darkPieceColor = Color(0xFF330000),
    lightPieceColor = Color(0xFFFF4444)
)

private val nordTheme = AppTheme(
    name = "Nord Aurora",
    backgroundMusic = "WiiParty",
    gameMusic = "MEGALOVANIA",
    placePieceSound = "putPiece",
    textColor = Color(0xFFECEFF4),
    buttonTextColor = Color(0xFF2E3440),
    backgroundColor = Color(0xFF2E3440),
    primaryColor = Color(0xFF88C0D0),
    secondaryColor = Color(0xFF3B4252),
    boardColor = Color(0xFF434C5E),
    boardBgColor = Color(0xFF2E3440),
    boardSideColor = Color(0xFF88C0D0),
    darkPieceColor = Color(0xFF4C566A),
    lightPieceColor = Color(0xFFD8DEE9)
)

private val polishCowTheme = AppTheme(
    name = "Polish Cow",
    backgroundMusic = "polish-cow",
    gameMusic = "polish-cow",
    placePieceSound = "putPiece",
    textColor = Color(0xFFFFFFFF),
    buttonTextColor = Color(0xFF000000),
    backgroundColor = Color(0xFF000000),
    backgroundImage = Res.drawable.polish_cow_background,
    primaryColor = Color(0xFFFFFFFF),
    secondaryColor = Color(0xFF1A1A1A),
    boardColor = Color(0xFF4F4F4F),
    boardBgColor = Color(0xFF000000),
    boardSideColor = Color(0xFFFFFFFF),
    darkPieceColor = Color(0xFF000000),
    lightPieceColor = Color(0xFFFFFFFF)
)

/**
 * Enumeration of all available application themes.
 * Each theme provides a complete color scheme and audio configuration.
 *
 * @property appTheme The AppTheme instance for this enumeration value.
 */
enum class AppThemes(val appTheme: AppTheme) {
    DARK(darkTheme),
    LIGHT(lightTheme),
    BENFICA(benficaTheme),
    MATRIX(matrixTheme),
    CYBERPUNK(cyberpunkTheme),
    NORD(nordTheme),
    POLISH_COW(polishCowTheme)
}