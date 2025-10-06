# LocalGDA – File Based Game Data Access

This folder provides the local filesystem implementation of the generic game data access contract. The implementation (`LocalGDA`) persists game state and chronological history (piece placements and pass actions) into a single, **human‑readable** plain text file. It favors simplicity, manual inspectability, and easy diffing during development.

## Goals & Characteristics
- Deterministic line‑oriented format (headers first, then history)
- Append‑friendly for moves and pass actions
- Domain validation expressed via structured `GDAResult` objects (no exceptions for normal cases)
- Independent of UI / CLI concerns
- Safe to parse incrementally; tolerant of extraneous future extension lines (ignored when unrecognized)

---
## File Format Specification
A persisted game file is UTF‑8 encoded with Unix line endings (platform default acceptable). Sections:

```
availablePieces: SYMBOL(|SYMBOL)*    // may be empty after colon
side: N                             // even integer 4..26
player: SYMBOL points playsLeft     // 0..2 lines, one per joined player
player: SYMBOL points playsLeft
piece: row col SYMBOL               // >= 0 lines, row & col 1-based indexes
piece: row col SYMBOL
pass: SYMBOL                        // optional pass lines (any order relative to pieces after headers)
pass: SYMBOL
```

Example after two players joined and three moves played plus a pass:
```
availablePieces:                    
side: 8
player: @ 12 20
player: # 15 18
piece: 4 4 @
piece: 4 5 #
piece: 5 5 @
pass: #
```
Meaning: Both piece types are taken (empty availablePieces). Board side is 8. White('@') and Black('#') players with current points + remaining plays (illustrative values). Three placements recorded; last action was a pass by '#'.

---
## Lifecycle & Typical Usage
1. Create and initialize a `GameImpl` with players & board.
2. Call `postGame(fileName, game)` to create/overwrite the file with headers + initial board snapshot.
3. After each legal move, call `postPiece(fileName, piece)`.
4. When a player passes, call `postPass(fileName, type)`.
5. To reconstruct current board later, call `getBoard(fileName)`.
6. To inspect move ordering or whose turn next (basic heuristic), fetch `getLatestPiece(fileName)` then apply game rules externally.

> Turn alternation (validation that the same player does not move twice) is intentionally NOT enforced inside `LocalGDA` to keep it stateless; higher‑level game logic should enforce rules.

---
## Public Operations Recap
| Method               | Purpose                                                   | Result Data (`data`)        |
|----------------------|-----------------------------------------------------------|-----------------------------|
| `postGame`           | Initialize / overwrite file with headers + board snapshot | `Boolean` (true on success) |
| `postPiece`          | Append a move line                                        | `Boolean`                   |
| `postPass`           | Append a pass line                                        | `Boolean`                   |
| `getBoard`           | Rebuild `Board` from piece lines                          | `Board`                     |
| `getAvailablePieces` | Remaining selectable piece types (symbols)                | `List<PieceType>`           |
| `getLatestPiece`     | Last chronological piece, or null                         | `Piece?`                    |

All methods return `GDAResult<T>` where:
```kotlin
val result: GDAResult<Boolean> = local.postPiece("game.txt", piece)
if (result.code == GDACodes.SUCCESS && result.data == true) { /* persisted */ }
else println(result.toStringColored())
```

---
## Result Codes Summary
- `SUCCESS` – Operation completed (payload usually meaningful)
- `DATA_NOT_FOUND` – File missing / empty / essential header absent
- `IO_ERROR` – Underlying IOException during read/write
- `SIDE_ERROR` – Missing or invalid `side` declaration
- `AVAILABLE_PIECES_ERROR` – Missing `availablePieces:` header when requested
- `BOARD_ERROR` – Malformed piece line encountered when explicitly parsing last piece
- `INVALID_FORMAT` / `UNKNOWN_ERROR` – Reserved for future nuanced parsing needs

---
## Error Handling Philosophy
No domain method throws for expected absence or validation failure. Instead, inspect `result.code`. This allows a CLI or service layer to map codes directly to user‑facing messages without try/catch noise. Only unexpected I/O faults are summarized into `IO_ERROR` with the originating exception message captured in `result.message`.

---
## Integration Example
```kotlin
val gda = LocalGDA()
val players = listOf(Player(PieceType.WHITE), Player(PieceType.BLACK))
val game = mockGame(gda, players, "demo.txt", Board(8), target = false, isLocal = true)

gda.postGame("demo.txt", game)
// Player WHITE plays (4,4):
gda.postPiece("demo.txt", Piece(Coordinate(4,4), PieceType.WHITE))
// Player BLACK passes:
gda.postPass("demo.txt", PieceType.BLACK)
// Load current board snapshot:
val boardResult = gda.getBoard("demo.txt")
if (boardResult.code == GDACodes.SUCCESS) println(boardResult.data) else println(boardResult.message)
```

---
## Concurrency & Atomicity
Writes are simple appends or full rewrites (for `postGame`). There is no locking; simultaneous writers can produce interleaved lines. If you need multi‑process safety, wrap operations with OS-level locks or replace `LocalGDA` with a transactional backend.

---
## Extensibility Notes
- Additional metadata (e.g., game end flag, timestamps) can be safely added as new prefixed lines; unknown prefixes are ignored by current parsing.
- To evolve format without breaking old files, keep existing prefixes stable and append new optional ones below player lines.

---
## Testing Strategy
Unit tests should treat the file as a black box: create a temporary file, call operations, then assert on `GDAResult` codes and inspect raw line content for structure correctness. Avoid depending on line ordering beyond specified header ordering.

---
## Known Limitations / Future Enhancements
- No validation of turn order or move legality (delegated to game logic layer)
- No automatic pruning/compaction (history only ever grows)
- Points / playsLeft are trusted as input; recalculation from board is not attempted
- Lack of checksum or corruption detection

---
## Summary
`LocalGDA` offers a pragmatic, transparent persistence mechanism ideal for local development, teaching scenarios, and lightweight tooling. Swap it out behind `GDAImpl` when moving to a networked or database-backed deployment without affecting the core game domain.

