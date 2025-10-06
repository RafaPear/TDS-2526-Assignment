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
| `pt.isel.reversi.core.game.data` | Generic persistence contracts + result codes / result wrapper |
| `pt.isel.reversi.core.game.localgda` | Concrete local text-file persistence (`LocalGDA`) |
| `pt.isel.reversi.cli` | CLI loop & rendering utilities |
| `pt.isel.reversi.cli.commands` | Discrete command handlers for user actions |

For in-depth narrative descriptions see `Module.md` (also surfaced in generated Dokka docs).

## Local Persistence (LocalGDA)
`LocalGDA` stores each game in a single UTF-8 text file with a deterministic, human-readable structure:
```
availablePieces: SYMBOL(|SYMBOL)*
side: N
player: SYMBOL points playsLeft
... (0..2 player lines)
piece: row col SYMBOL
pass: SYMBOL
```
Moves and passes are appended; board reconstruction parses only `piece:` lines and validates `side:`. Turn order validation is left to higher-level logic.

Detailed usage, file format specification, lifecycle, and extensibility notes live in:
`src/main/kotlin/pt/isel/reversi/core/game/localgda/README.MD`

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
- Header creation & overwrite
- Piece / pass recording
- Error codes for missing files & structural anomalies

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
