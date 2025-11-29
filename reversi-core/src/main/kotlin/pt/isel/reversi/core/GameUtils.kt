package pt.isel.reversi.core

import pt.isel.reversi.core.board.Board
import pt.isel.reversi.core.board.Coordinate
import pt.isel.reversi.core.board.PieceType
import pt.isel.reversi.core.exceptions.*
import pt.isel.reversi.core.storage.GameState

/**
 * Starts a new game.
 * It is recommended to use this method only to create a not local game.
 * If is not a local game makes available the opponent player in storage for future loads.
 * @param side The side length of the board.
 * @param players The list of players.
 * @param firstTurn The piece type of the player who goes first can omit to use the default.
 * @param currGameName The current game name can omit to create a local game.
 * @return The new game state.
 * @throws InvalidGameException if no players are provided.
 * @throws Exception if already exists a game with the same name in storage.
 */
fun startNewGame(
    side: Int = loadCoreConfig().BOARD_SIDE,
    players: List<Player>,
    firstTurn: PieceType,
    currGameName: String? = null,
): Game {
    if (players.isEmpty())
        throw InvalidGameException(
            "Need minimum one player to start the game",
            ErrorType.WARNING
        )

    val board = Board(side).startPieces()

    val gs = GameState(
        board = board,
        players = players.map { it.refresh(board) },
        lastPlayer = firstTurn.swap(),
        winner = null
    )

    if (currGameName != null && gs.players.size == 1) {
        val newGS = gs.copy(
            players = listOf(gs.players[0].swap().refresh(board)),
        )

        try {
            return Game(
                target = false,
                gameState = gs,
                currGameName = currGameName,
            ).also { it.storage.new(currGameName) { newGS } }
        } catch (_: Exception) {
            throw InvalidNameAlreadyExists(
                message = "A game with the name '$currGameName' already exists.",
                type = ErrorType.WARNING
            )
        }
    }

    return Game(
        target = false,
        gameState = gs,
        currGameName = currGameName,
    )
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
fun loadGame(
    gameName: String,
    desiredType: PieceType? = null,
): Game {
    val conf = loadCoreConfig()
    val storage = conf.STORAGE_TYPE.storage(conf.SAVES_FOLDER)
    val loadedState = storage.load(gameName)
                      ?: throw InvalidFileException(
                          message = "$gameName does not exist",
                          type = ErrorType.ERROR
                      )

    val myPieceType =
        if (loadedState.players.isNotEmpty())
            desiredType ?: loadedState.players[0].type
        else
            throw InvalidPieceInFileException(
                message = "No players available in the loaded game: $gameName.",
                type = ErrorType.ERROR
            )

    val newState = loadedState.copy(
        players = loadedState.players.find { it.type == myPieceType }?.let {
            listOf(it)
        } ?: throw InvalidPieceInFileException(
            message = "Player with piece type ${myPieceType.symbol} is not available in the loaded game: $gameName.",
            type = ErrorType.WARNING
        ),
    )

    val opponents = loadedState.players.filter { it.type != myPieceType }

    storage.save(
        id = gameName,
        obj = newState.copy(
            players = opponents
        )
    )

    return Game(
        target = false,
        gameState = newState.copy(
            players = newState.players.map { it.refresh(newState.board) }
        ),
        currGameName = gameName,
    )
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
                row == 0             -> sb.append("$col ")
                col == 0             -> sb.append("$row ")
                else                 -> sb.append(
                    when (useTarget && cords in availablePlays) {
                        true -> "${this.config.TARGET_CHAR} "
                        false -> (board[cords]?.symbol ?: this.config.EMPTY_CHAR) + " "
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
    players: List<Player>,
    lastPlayer: PieceType,
    currGameName: String? = null,
): Game = Game(
    target = false,
    currGameName = currGameName,
    gameState = GameState(
        board = board,
        players = players,
        lastPlayer = lastPlayer,
        winner = null
    )
)
