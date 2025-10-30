Module CLI

This module implements the core domain logic and persistence contracts.

#Package pt.isel.reversi.cli

## Overview

Hosts the command‑line interface: parses user input, renders board snapshots, and routes validated commands to the game
services. Designed to be scriptable and testable using standard IO.

### Responsibilities

- Parsing commands and arguments
- Rendering game state and helpful messages
- Orchestrating the interaction loop

#Package pt.isel.reversi.cli.commands

## Overview

Groups discrete user operations (e.g., start/new, join/load, move, pass, help, quit) under cohesive command objects.
Emphasizes clear feedback and argument validation while delegating game rules to the core.

### Responsibilities

- Command parsing and validation
- Error reporting with actionable guidance
- Extensibility for new commands