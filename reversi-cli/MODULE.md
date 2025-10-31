Module reversi-cli

This module is responsible for the command‑line interface of the Reversi game. It handles user input parsing, game state rendering, and command routing to the core game services. This design is based on the library [KtFlag](https://github.com/rafapear/KtFlag) for command parsing. THis library removes a lot of boilerplate code and allows us to focus on the core logic of the CLI. In the bellows sections, we detail the main packages.

![Cli Architecture](../images/UML_Structure_CLI.png)

#Package pt.isel.reversi.cli

## Overview

Hosts the command‑line interface: parses user input, renders board snapshots, and routes validated commands to the game
services. Designed to be scriptable and testable using standard IO.


#Package pt.isel.reversi.cli.commands

## Overview

Defines the command structure, parsing logic, and validation for user inputs. Each command corresponds to a specific game
action (e.g., start, move, pass, show). Utilizes KtFlag to streamline argument parsing and error handling.

## Example of a command

```kotlin
object ExampleCmd : CommandImpl<GameImpl>() {
    override val info: CommandInfo = CommandInfo(
        title = "Example Command",
        description = "An example command that demonstrates structure.",
        longDescription = "This command serves as a template for creating new commands.",
        aliases = listOf("ex", "example"),
        usage = "example",
        minArgs = 0,
        maxArgs = 0
    )

    override fun execute(
        vararg args: String,
        context: GameImpl?
    ): CommandResult<GameImpl> {
        println("Executing example command...")

        return CommandResult.SUCCESS("Example command executed successfully", context)
    }

}
```

This structure allows for easy addition of new commands by defining their info and execution logic.

> ⚠️ **Note:** If you want to better explore `KtFlag`, check the [KtFlag repository](https://github.com/rafapear/KtFlag)
