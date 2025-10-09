# Reversi (Othello) Kotlin Implementation

A modular, test-friendly implementation of the Reversi board game written in Kotlin (JVM). The project emphasizes clean separation of concerns, explicit domain modeling, and pluggable persistence.

## Contents
- Core immutable board & piece model
- Game abstraction (players, logic extension points)
- Local filesystem persistence (text format) via `LocalGDA`
- CLI shell with commands (start, join, move, pass, help, quit)
- Structured result codes for persistence feedback
- Unit tests (board + data access layer)
- Generated documentation (Dokka) with curated package overviews

## Quick Start
```bash
# Build (includes fat JAR and docs)
./gradlew build

# Run (fat jar contains dependencies)
java -jar build/libs/Reversi-0.0.1-all.jar
```
On Windows (cmd):
```cmd
gradlew.bat build
java -jar build\libs\Reversi-0.0.1-all.jar
```

## Architecture Overview
| Package | Responsibility |
|---------|----------------|
| `pt.isel.reversi.core.board` | Board, coordinates, piece types & safe transformation operations |
| `pt.isel.reversi.core.game` | Game interfaces, player representation, logic contracts |
| `pt.isel.reversi.core.game.localgda` | Concrete local text-file persistence (`LocalGDA`) |
| `pt.isel.reversi.cli` | CLI loop & rendering utilities |
| `pt.isel.reversi.cli.commands` | Discrete command handlers for user actions |

For in-depth narrative descriptions see `Module.md` (also surfaced in generated Dokka docs).

## Local Persistence (LocalGDA)
`LocalGDA` stores each game in a single UTF-8 text file with a deterministic, human-readable structure. The file is line-oriented and intentionally simple so it can be inspected and edited by hand if needed.

Supported line kinds (order is flexible for headers; moves/passes are chronological):
```
availablePieces: SYMBOL(|SYMBOL)*   # available piece symbols separated by '|', e.g. "#|@"
side: N                              # integer board side (even, e.g. 8)
player: SYMBOL points playsLeft      # optional player lines
piece: row col SYMBOL                # a played piece; row and col are integers
pass: SYMBOL                         # a pass for the player with the given symbol
```

Key behaviour notes:
- postGame has two distinct code paths:
  1. New file (or empty file): `postGame` will create the file and write a full snapshot consisting of `availablePieces` followed by `side` and a serialized snapshot of the current board as `piece:` lines. Available pieces are computed by removing the game players' types from the full set of PieceType entries.
  2. Existing file: `postGame` first reads the existing headers using `GameFileAccess.readGameInfo`. If the persisted `side` does not match `game.board.side` an `InvalidGameWriteException` is thrown — this prevents unintended corruption. If sides match, the persisted `availablePieces` header is filtered by the players in the supplied `game` and the file is overwritten with the updated snapshot.

- postPiece and postPass append a single `piece:` or `pass:` line respectively. These are intended to be cheap, append-only operations.

- getBoard reconstructs the board by reading headers (verifying `side`) and parsing the chronological `piece:` lines. Malformed piece lines will surface as `InvalidPieceInFileException`.

Exceptions and expected errors (domain-level):
- `InvalidSideInFileException`: thrown when the `side:` header is missing or not a valid integer. This is specifically about the *side* header and should only be used for that purpose.
- `InvalidAvailablePiecesInFileException`: thrown when the `availablePieces:` header is missing or malformed.
- `InvalidPieceInFileException`: thrown when a `piece:` line cannot be parsed (wrong token count, non-integer coordinates, unknown symbol).
- `InvalidGameWriteException`: thrown by `LocalGDA.postGame` when attempting to overwrite an existing file with an incompatible game state (for now, a mismatched `side`).

The data access helpers live in `GameFileAccess` which contains focused parsing/writing routines and throws the above exceptions in the correct, domain-focused places.

## Result Handling Philosophy
Persistence methods return `GDAResult<T>` instead of throwing for expected states:
```kotlin
val r = gda.postPiece("game.txt", piece)
when (r.code) {
    GDACodes.SUCCESS -> println("Saved: ${r.data}")
    GDACodes.DATA_NOT_FOUND -> println("Missing file: ${r.message}")
    else -> println("Issue: ${r.code} -> ${r.message}")
}
```
This makes the CLI straightforward and enables richer tooling (e.g., colored outputs) without exception noise.

## Testing
Run the test suite:
```bash
./gradlew test
```
Board tests validate coordinate & piece placement invariants. Data access tests assert:
- Header creation & overwrite (both code paths in `postGame`)
- Piece / pass recording
- Error codes / exceptions for missing files & structural anomalies

Add new tests under `src/test/kotlin/` following existing minimal assertion style.

## Extending the Project
| Goal | Suggested Approach |
|------|--------------------|
| Add AI player | Implement strategy inside a new logic class conforming to `GameLogicImpl` (future) |
| Remote persistence | Provide another `GDAImpl` (e.g., HTTP or DB) and inject at bootstrap |
| Rich CLI feedback | Leverage `toStringColored()` on results; add command output adapters |
| GUI / Web UI | Reuse core & data modules; replace CLI loop with UI event handlers |

## Documentation
Generate updated docs:
```bash
./gradlew dokkaHtml
```
Output: `build/dokka/html/index.html`

## Design Principles Recap
- Immutability for domain objects (safer reasoning & potential concurrency)
- Explicit outcomes over exceptions for predictable domain states
- Append-only, transparent persistence for easy debugging
- Dependency injection boundary at game + data access interface layer

## Future Improvements (Backlog Ideas)
- Implement full move legality & flipping logic integration
- Endgame detection & scoring summary generator
- Turn order enforcement in a higher-level service
- Optional timestamp metadata in persistence format
- Progressive compaction (e.g., periodic board snapshots)

## Contributing
1. Fork & branch (`feature/description`)
2. Add/adjust tests
3. Ensure `./gradlew build` passes (including Dokka & fat jar)
4. Create PR summarizing motivation & approach

## License
(Choose a license and place it in a `LICENSE` file—currently unspecified.)

---
Happy hacking! Explore `LocalGDA` for a clear example of how to integrate alternative persistence layers without touching core game logic.
