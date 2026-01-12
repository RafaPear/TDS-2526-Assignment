package pt.isel.reversi.core

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.*
import pt.isel.reversi.core.storage.GameState
import pt.isel.reversi.core.storage.GameStorageType.Companion.setUpStorage
import pt.isel.reversi.core.storage.MatchPlayers

fun loadStorageFromConfig() = setUpStorage(loadCoreConfig())

/**
 * Starts a new game.
 * It is recommended to use this method only to create a not local game.
 * If is not a local game makes available the opponent player in storage for future loads.
 * @param side The side length of the board (required).
 * @param players The list of players.
 * @param firstTurn The piece type of the player who goes first can omit to use the default.
 * @param currGameName The current game name can omit to create a local game.
 * @return The new game state.
 * @throws InvalidGameException if no players are provided.
 * @throws InvalidNameAlreadyExists if already exists a game with the same name in storage.
 */
suspend fun startNewGame(
    side: Int,
    players: MatchPlayers,
    firstTurn: PieceType,
    currGameName: String? = null,
): Game {
    if (players.isEmpty()) throw InvalidGameException(
        "Need minimum one player to start the game", ErrorType.WARNING
    )

    val board = Board(side).startPieces()

    val gs = GameState(
        players = players.refreshPlayers(board),
        lastPlayer = firstTurn.swap(),
        board = board,
        winner = null
    )

    return if (currGameName != null) {
        try {
            Game(
                target = false,
                gameState = gs,
                currGameName = currGameName,
                myPiece = firstTurn,
            ).also { it.storage.new(currGameName) { gs } }
        } catch (_: Exception) {
            throw InvalidNameAlreadyExists(
                message = "A game with the name '$currGameName' already exists.", type = ErrorType.WARNING
            )
        }
    } else {
        Game(
            target = false,
            gameState = gs,
            currGameName = currGameName,
            myPiece = firstTurn,
        )
    }
}

/**
 * Loads an existing game from storage.
 * It is recommended to use this method only connecting to a not local game.
 * Ensures that the player with the specified piece type is included in the loaded game.
 * Removes the player from storage to avoid conflicts in future loads.
 * @param gameName The name of the game to load.
 * @return The loaded game state.
 * @throws InvalidFileException if there is an error loading the game state.
 * @throws InvalidPieceInFileException if the specified piece type is not found in the loaded game.
 */
suspend fun loadGame(
    gameName: String,
    playerName: String? = null,
    desiredType: PieceType?,
): Game {
    val storage = loadStorageFromConfig()
    val loadedState = storage.load(gameName)
        ?: throw InvalidFileException(
            message = "$gameName does not exist",
            type = ErrorType.ERROR
        )

    val myPieceType = desiredType ?: loadedState.players.getFreeType()
    ?: throw InvalidPieceInFileException(
        message = "No available piece types in the loaded game: $gameName.",
        type = ErrorType.WARNING
    )

    val player = Player(type = myPieceType, name = playerName ?: myPieceType.name)

    val newMatch = loadedState.players.addPlayerOrNull(player) ?: throw InvalidPieceInFileException(
        message = "Player with piece type ${myPieceType.symbol} is not available in the loaded game: $gameName.",
        type = ErrorType.WARNING
    )

    val newState = loadedState.copy(players = newMatch)

    storage.save(
        id = gameName,
        obj = newState
    )

    return Game(
        target = false,
        gameState = newState.copy(
            players = newState.players.refreshPlayers(newState.board),
        ),
        currGameName = gameName,
        myPiece = myPieceType,
    )
}

suspend fun readGame(gameName: String): Game? {
    val storage = loadStorageFromConfig()
    val loadedState = storage.load(gameName) ?: return null

    return Game(
        target = false,
        gameState = loadedState.copy(
            players = loadedState.players.refreshPlayers(loadedState.board),
        ),
        currGameName = gameName,
        myPiece = null,
    )
}

suspend fun readState(gameName: String): GameState? {
    val storage = loadStorageFromConfig()
    return storage.load(gameName)
}

fun Game.stringifyBoard(): String {
    val sb = StringBuilder()
    val board = gameState?.board ?: return "Board not initialized"
    val availablePlays = if (target) getAvailablePlays() else null
    val useTarget = availablePlays != null

    for (row in 0..board.side) {
        for (col in 0..board.side) {
            val cords = Coordinate(row, col)
            when {
                row == 0 && col == 0 -> sb.append("  ")
                row == 0 -> sb.append("$col ")
                col == 0 -> sb.append("$row ")
                else -> sb.append(
                    when (useTarget && cords in availablePlays) {
                        true -> "${this.config.targetChar} "
                        false -> (board[cords]?.symbol ?: this.config.emptyChar) + " "
                    }
                )
            }
        }
        sb.appendLine()
    }
    return sb.toString()
}

fun newGameForTest(
    board: Board,
    players: MatchPlayers,
    myPiece: PieceType,
    currGameName: String? = null,
): Game = Game(
    target = false,
    currGameName = currGameName,
    myPiece = myPiece,
    gameState = GameState(
        players = players,
        lastPlayer = myPiece,
        board = board,
        winner = null
    )
)

suspend fun getAllGameNames(): List<String> {
    val storage = loadStorageFromConfig()
    return storage.loadAllIds()
}
