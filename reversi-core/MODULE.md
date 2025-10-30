Module Core

This module defines the core domain logic and interfaces. It is agnostic to the UI and persistence layers.

#Package pt.isel.reversi.core.board

## Overview

Models the board (grid), coordinates, and piece placement rules. Provides operations to evaluate and apply moves, flip
affected pieces, and derive the initial setup according to standard Reversi. Validation ensures moves are within bounds
and conform to the game rules.

### Responsibilities

- Coordinate and bounds validation
- Legal move evaluation and piece flipping
- Producing updated board states from moves
- Getting the initial board setup

#Package pt.isel.reversi.core.game

## Overview

Defines the high‑level game orchestration (turns, move validation, pass, termination) and the composition of core
elements (board, players). Remains UI‑agnostic and persistence‑agnostic, exposing a small set of operations to control
the game flow.

### Responsibilities

- Tracking current player and turn alternation
- Applying moves or passes and updating the state
- Detecting end of game and computing scores
- Exposing game snapshots for presentation

#Package pt.isel.reversi.core.game.data

## Overview

Encapsulates contracts for persistence and data transfer. Specifies how game state and events are
serialized/deserialized and how operations report outcomes using typed results suitable for user feedback and tooling.

### Responsibilities

- Data contracts for storing/loading game state
- Mapping between domain objects and serialized forms
- Validating data shape and reporting structured results

#Package pt.isel.reversi.core.game.localgda

## Overview

Provides a concrete local filesystem implementation for the data access contracts, using a simple, human‑readable text
format. Intended for development and testing; replaceable by alternative implementations without changing domain logic.

### Responsibilities

- Reading/writing game data from/to text files
- Validating file structure and contents during I/O
- Reporting operation results and error details

#Package pt.isel.reversi.core.game.exceptions

## Overview

Defines domain‑specific exceptions to signal rule violations or invalid operations in a controlled manner, keeping error
handling separate from the normal flow.

### Examples

- Invalid move or pass attempts
- Attempts to load or operate on unavailable game data