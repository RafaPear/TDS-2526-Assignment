Module Reversi

This module implements the Reversi game with a focus on modularity, clarity, and extensibility. The architecture is divided into distinct packages to separate responsibilities, facilitating maintenance, testing, and project evolution.

It provides a clean core domain (board, players, game state & logic), a CLI layer, and a simple file‑based persistence component.

#Package pt.isel.reversi.core.board
## Overview
Models the board (grid), coordinates and piece placement rules. Provides operations to evaluate and apply moves, flip affected pieces, and derive the initial setup according to standard Reversi. Validation ensures moves are within bounds and conform to the game rules.

### Responsibilities
- Coordinate and bounds validation
- Legal move evaluation and piece flipping
- Producing updated board states from moves
- Obtaining the initial board setup

#Package pt.isel.reversi.core.game
## Overview
Defines the high‑level game orchestration (turns, move validation, pass, termination) and the composition of core elements (board, players). Remains UI‑agnostic and persistence‑agnostic, exposing a small set of operations to control the game flow.

### Responsibilities
- Tracking current player and turn alternation
- Applying moves or passes and updating state
- Detecting end of game and computing scores
- Exposing game snapshots for presentation

#Package pt.isel.reversi.core.game.data
## Overview
Encapsulates contracts for persistence and data transfer. Specifies how game state and events are serialized/deserialized and how operations report outcomes using typed results suitable for user feedback and tooling.

### Responsibilities
- Data contracts for storing/loading game state
- Mapping between domain objects and serialized forms
- Validating data shape and reporting structured results

#Package pt.isel.reversi.core.game.localgda
## Overview
Provides a concrete local filesystem implementation for the data access contracts, using a simple, human‑readable text format. Intended for development and testing; replaceable by alternative implementations without changing domain logic.

### Responsibilities
- Reading/writing game data from/to text files
- Validating file structure and contents during I/O
- Reporting operation results and error details

#Package pt.isel.reversi.cli
## Overview
Hosts the command‑line interface: parses user input, renders board snapshots, and routes validated commands to the game services. Designed to be scriptable and testable using standard IO.

### Responsibilities
- Parsing commands and arguments
- Rendering game state and helpful messages
- Orchestrating the interaction loop

#Package pt.isel.reversi.cli.commands
## Overview
Groups discrete user operations (e.g., start/new, join/load, move, pass, help, quit) under cohesive command objects. Emphasizes clear feedback and argument validation while delegating game rules to the core.

### Responsibilities
- Command parsing and validation
- Error reporting with actionable guidance
- Extensibility for new commands

#Package pt.isel.reversi
## Overview
Provides the application entry point and composition root: wires dependencies, selects implementations (e.g., local data access), and starts the CLI loop. Acts as the integration point for alternative front‑ends.

### Responsibilities
- Application bootstrap and configuration
- Dependency wiring between layers
- Starting and stopping the runtime

#Package pt.isel.reversi.core.game.exceptions
## Overview
Defines domain‑specific exceptions to signal rule violations or invalid operations in a controlled manner, keeping error handling separate from the normal flow.

### Examples
- Invalid move or pass attempts
- Attempts to load or operate on unavailable game data

---

## Design Notes
- Immutability first: operations produce new states rather than mutating shared state, simplifying reasoning and testing.
- Small surface area: interfaces focus on intent to enable alternative implementations (AI, remote persistence) without changing callers.
- Explicit outcomes: prefer typed results for expected conditions and exceptions for truly exceptional failures.
- Testability: separation of concerns and dependency inversion enable unit and integration testing across layers.
- Extensibility: pluggable persistence and logic strategies without impacting the core domain.

