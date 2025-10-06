# Module Reversi

This module implements the Reversi game with a focus on modularity, clarity, and extensibility. The architecture is divided into distinct packages to separate responsibilities, facilitating maintenance, testing, and project evolution.

It provides a clean core domain (board, players, game state & logic), an extensible CLI layer, and a simple file‑based persistence component. Each package below is declared with a #package marker consumed by Dokka, immediately followed by a concise Markdown heading (title without the fully qualified name) and a narrative description (no code examples) to support conceptual navigation in generated documentation.

#package pt.isel.reversi.core.board
# Board
Models the reversible grid state and immutable operations over it. The board exposes safe value semantics (data class) and transformation methods that either yield a new board or throw when preconditions are violated. Responsibilities include:
- Coordinate normalization and validation (1-based rows, alpha or numeric columns)
- Piece placement and color flipping with invariant checks (even side, bounds, uniqueness)
- Deriving initial setup (standard four center pieces) without embedding game turn logic

#package pt.isel.reversi.core.game
# Game
Defines the high‑level game abstraction (composition of board, players, and access layer) plus extension points for logic strategies. It coordinates player actions, target/assist modes and refresh/pass mechanics while remaining UI‑agnostic and persistence‑agnostic. Lifecycle concerns (start, join, play, pass) are modeled to allow future remote or AI player integrations.

#package pt.isel.reversi.core.game.data
# Data Access
Encapsulates persistence contracts and a lightweight local file implementation using a line‑oriented plain text format. It standardizes outcome reporting with typed result codes and colored CLI formatting while remaining decoupled from the core game rules. Responsibilities include translating domain state to/from a canonical textual representation and validating structural integrity (headers, piece chronology, turn alternation hints).

#package pt.isel.reversi.core.game.localgda
# Local GDA
Implements the concrete local filesystem persistence (text file format) for the generic data access abstraction. Complements the `pt.isel.reversi.core.game.data` contracts by providing a human‑readable, append‑oriented storage strategy used by the CLI and tests. Suitable for development and teaching; replaceable by alternative implementations (network / database) without touching domain logic.

#package pt.isel.reversi.cli
# CLI
Hosts the command‑line presentation & interaction layer: parsing user intent, rendering board snapshots, and routing validated commands into the game abstraction. It focuses on progressive disclosure of information (e.g., optional target hints) and aims to remain scriptable and testable without side‑effects beyond standard IO.

#package pt.isel.reversi.cli.commands
# Commands
Groups discrete user operations (start, join, move, pass, help, quit) under cohesive command objects. Emphasizes clear feedback, argument validation, and future extensibility (adding commands without modifying existing logic). Avoids embedding persistence or rendering concerns directly.

#package pt.isel.reversi
# Core Entry
Provides the application bootstrap: wiring dependencies, selecting configured implementations (e.g., local data access), and delegating control to the CLI loop. Serves as the integration point for embedding this module into alternate front‑ends (GUI, web service) or automated test harnesses.

---

## Design Notes
- Immutability: Core structures (Board, Piece) return new instances, simplifying reasoning and potential concurrency.
- Small Surface: Interfaces (GameImpl, GameLogicImpl, GameDataAccessImpl) concentrate on intent, enabling alternative implementations (AI strategies, remote persistence) without refactoring callers.
- Explicit Outcomes: Data access uses structured result codes rather than exceptions for expected domain states (missing data, invalid format), aiding CLI feedback & tooling.
- Progressive Enhancement: The current feature set is a foundation; logic expansion (legal move generation, scoring, endgame detection) is intentionally modular.
