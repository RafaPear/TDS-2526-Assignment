package pt.isel.reversi.app.state

import pt.isel.reversi.app.AppTheme
import pt.isel.reversi.app.pages.PagesState
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.gameServices.GameServiceImpl
import pt.isel.reversi.utils.audio.AudioPool

interface AppStateImpl {
    val gameSession: GameSession
    val pagesState: PagesState
    val globalError: ReversiException?
    val service: GameServiceImpl
    val audioThemeState: AudioThemeState
    val game: Game get() = this.gameSession.game
    val playerName: String? get() = this.gameSession.playerName
    val audioPool: AudioPool get() = this.audioThemeState.audioPool
    val theme: AppTheme get() = this.audioThemeState.theme
}