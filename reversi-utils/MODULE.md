Module reversi-utils

This module groups reusable utilities used by the various Reversi modules (core, cli, storage, ...). It provides helpers
for loading configuration, environment constants and simple contracts to ease initialization and reading/writing
configuration files.

<img src="../images/UML_Structure_utils.png" alt="Utils Architecture" width="18%"/>

#Package pt.isel.reversi.utils

## Overview

This package contains reusable utilities that support configuration handling.
The central feature is a `ConfigLoader` class that automates the creation and loading of configuration files in
`.properties` format, allowing each subsystem to define its own configuration model by implementing the `Config`
interface.
This ensures that each module can provide default configuration values while still allowing users to override them
through editable files.

- `ConfigLoader<U : Config>`
    - Reads a properties file from a provided path.
    - If the file does not exist, creates the directory/file; writes default entries obtained from
      `factory(emptyMap()).getDefaultConfigFileEntries()` and stores the file.
    - Converts the loaded properties to `Map<String, String>` and invokes `factory(configMap)` to build the concrete
      configuration.
- `Config`
    - Contract that requires exposing `getDefaultConfigFileEntries(): Map<String, String>` with the default values to
      write when the file does not exist.
- `Environment`
    - Defines constants: `CONFIG_FOLDER`, `CORE_CONFIG_FILE`, `CLI_CONFIG_FILE`. The default directory is config and the
      default files are `reversi-core.properties` and `reversi-cli.properties`.

### Responsibilities

- Provide a simple and consistent mechanism to load/manage configuration files.
- Ensure missing configuration files are created with default entries.
- Expose reusable environment constants for other modules to locate configuration files.

### Notes

- Modules such as `reversi-core` and `reversi-cli` should provide an implementation of `Config` and pass a factory to
  `ConfigLoader`.
- The default configuration file is plain text in Java Properties format, as this facilitates manual editing and
  debugging.