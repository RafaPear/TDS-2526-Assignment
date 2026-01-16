package pt.isel.reversi.app.app.state

import pt.isel.reversi.app.app.AppTheme
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.core.game.Game
import pt.isel.reversi.core.game.gameServices.GameServiceImpl
import pt.isel.reversi.utils.audio.AudioPool

interface AppStateImpl {
    val gameSession: GameSession
    val pagesState: PagesState
    val service: GameServiceImpl
    val audioThemeState: AudioThemeState
    val game: Game get() = this.gameSession.game
    val playerName: String? get() = this.gameSession.playerName
    val audioPool: AudioPool get() = this.audioThemeState.audioPool
    val theme: AppTheme get() = this.audioThemeState.theme
    val globalError: ReversiException? get() = this.pagesState.globalError
}