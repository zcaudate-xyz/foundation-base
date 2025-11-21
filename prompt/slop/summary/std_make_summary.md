## std.make: A Comprehensive Summary (including submodules)

The `std.make` module provides a comprehensive framework for automating build processes, managing project configurations, and interacting with version control systems (specifically Git and GitHub). It aims to simplify the creation and maintenance of `Makefile`s, manage project dependencies, and streamline common development workflows like building, testing, and releasing software. The module is designed to be extensible, allowing for custom build steps and integration with various tools.

### `std.make` (Main Namespace)

This namespace serves as the primary entry point for the build automation system, aggregating and re-exporting key functionalities from its submodules. It provides high-level functions and macros for common build tasks.

**Key Re-exported Functions:**

*   From `std.make.compile`: `with:mock-compile`, `types:add`, `types:list`, `types:remove`, `build`.
*   From `std.make.common`: `with:internal-shell`, `triggers-clear`, `triggers-get`, `triggers-list`, `triggers-purge`, `triggers-set`, `get-triggered`, `make-config`, `dir`, `dir:exists?`, `dir:teardown`, `run:shell`, `run`, `run-close`, `run-internal`, `run:init`, `run:package`, `run:dev`, `run:test`, `run:release`, `run:start`, `run:stop`.
*   From `std.make.github`: `dir:repo-rebuild`, `dir:repo-setup`, `gh:token`, `gh:user`, `gh:exists?`, `gh:commit`, `gh:push`, `gh:save`, `gh:clone`, `gh:setup`, `gh:setup-init`, `gh:setup-remote`, `gh:local-purge`, `gh:dwim-init`, `gh:dwim-push`, `with-verbose`.
*   From `std.make.project`: `def.make`, `build-all`, `build-at`, `build-default`, `build-triggered`, `is-changed?`.
*   From `std.make.readme`: `org:tangle`, `org:readme`.
*   From `std.make.bulk`: `bulk-container-build`, `bulk-container-filter`, `bulk-build`, `bulk`, `bulk-gh-init`, `bulk-gh-push`.

### `std.make.bulk` (Bulk Operations)

This sub-namespace provides functions for performing bulk operations across multiple projects or configurations, such as building multiple containers or pushing changes to multiple GitHub repositories.

**Key Functions:**

*   **`make-bulk-get-keys`**: Determines the order of projects to build based on dependencies and changes.
*   **`make-bulk-build`**: Builds multiple projects in bulk.
*   **`make-bulk`**: Orchestrates a bulk build process, including logging and timing.
*   **`make-bulk-container-filter`**: Filters configurations based on container names.
*   **`make-bulk-container-build`**: Builds multiple containers in bulk.
*   **`make-bulk-gh-init`**: Initializes multiple GitHub repositories in bulk.
*   **`make-bulk-gh-push`**: Pushes changes to multiple GitHub repositories in bulk.

### `std.make.common` (Common Makefile Utilities)

This sub-namespace provides shared helper functions and dynamic variables for managing `Makefile` configurations, running shell commands, and handling triggers.

**Core Concepts:**

*   **`*triggers*`**: An atom storing a map of configurations to their associated triggers.
*   **`*tmux*`**: A dynamic var to control whether `tmux` is used for running commands.
*   **`*internal-shell*`**: A dynamic var to control whether commands are run in an internal shell.
*   **`MakeConfig` Record:** Represents a `Makefile` configuration.

**Key Functions:**

*   **`with:triggers` (macro)**: Binds the `*triggers*` atom.
*   **`triggers-purge`, `triggers-set`, `triggers-clear`, `triggers-get`, `triggers-list`**: Functions for managing triggers.
*   **`get-triggered`**: Retrieves configurations based on a trigger namespace.
*   **`with:internal-shell` (macro)**: Binds `*internal-shell*`.
*   **`make-config-string`**: Returns a string representation of a `MakeConfig`.
*   **`make-config?`**: Checks if an object is a `MakeConfig`.
*   **`get-config-tag`**: Retrieves the tag of a `MakeConfig`.
*   **`make-config-defaults`**: Returns default `MakeConfig` settings.
*   **`make-config-map`**: Creates a `MakeConfig` map.
*   **`make-config`**: Creates a `MakeConfig` instance.
*   **`make-config-update`**: Updates a `MakeConfig`.
*   **`make-dir`**: Returns the build directory for a `MakeConfig`.
*   **`make-run`**: Runs a `make` command.
*   **`make-run-close`**: Closes a `tmux` window.
*   **`make-run-internal`**: Runs `make` commands internally.
*   **`make-shell`**: Opens a terminal in the build directory.
*   **`make-run-init`, `make-run-package`, `make-run-release`, `make-run-dev`, `make-run-test`, `make-run-start`, `make-run-stop`**: Specific `make` commands.
*   **`make-dir-setup`**: Sets up the build directory.
*   **`make-dir-exists?`**: Checks if the build directory exists.
*   **`make-dir-teardown`**: Deletes the build directory.

### `std.make.compile` (Compilation Utilities)

This sub-namespace provides utilities for compiling various types of files and resources, including custom formats, directories, and language-specific modules.

**Core Concepts:**

*   **`*mock-compile*`**: A dynamic var to enable mock compilation for testing.
*   **Compilation Types:** Supports `:resource`, `:directory`, `:custom`, `:script`, `:module.graph`, `:module.single`, `:module.schema`.

**Key Functions:**

*   **`with:mock-compile` (macro)**: Enables mock compilation.
*   **`compile-fullbody`**: Combines header, body, and footer into a full body.
*   **`compile-out-path`**: Generates the output path for a compiled file.
*   **`compile-write`**: Writes compiled content to a file.
*   **`compile-summarise`**: Summarizes compilation results.
*   **`compile-resource`**: Copies resources to the build directory.
*   **`compile-directory`**: Copies a directory to the build directory.
*   **`compile-custom`**: Compiles custom content.
*   **`types-list`, `types-add`, `types-remove`**: Manages compilation types.
*   **`compile-ext-fn` (multimethod)**: Extensible function for compiling different file extensions (e.g., `:blank`, `:raw`, `:edn`, `:json`, `:yaml`, `:toml`, `:html`, `:css`, `:sql`, `:redis`, `:vega`, `:gnuplot`, `:graphviz`, `:gitignore`, `:nginx.conf`, `:dockerfile`, `:readme.md`, `:makefile`, `:package.json`).
*   **`compile-resolve`**: Resolves a symbol or pointer.
*   **`compile-ext`**: Compiles files of different extensions.
*   **`compile-single`**: Compiles a single file.
*   **`compile-section`**: Compiles a section of a `Makefile`.
*   **`compile-directive`**: Compiles a directive within a `Makefile`.
*   **`compile`**: The main function for compiling files based on a `MakeConfig`.

### `std.make.github` (GitHub Integration)

This sub-namespace provides functions for interacting with GitHub repositories, including committing, pushing, cloning, and setting up remote repositories.

**Core Concepts:**

*   **`*verbose*`**: A dynamic var to control verbose output for Git commands.

**Key Functions:**

*   **`with-verbose` (macro)**: Controls verbose output.
*   **`gh-sh-opts`**: Creates standard shell options for Git commands.
*   **`gh-commit`**: Creates a Git commit.
*   **`gh-push`**: Pushes changes to GitHub.
*   **`gh-save`**: Commits and pushes changes.
*   **`gh-user`, `gh-token`**: Retrieves GitHub user and token from environment variables.
*   **`gh-exists?`**: Checks if a GitHub repository exists.
*   **`gh-setup-remote`**: Creates a remote GitHub repository.
*   **`gh-setup-local-init`**: Initializes a local Git repository and links it to a remote.
*   **`gh-setup-local-clone`**: Clones a GitHub repository.
*   **`gh-clone`**: Forces a Git clone.
*   **`gh-setup`**: Sets up a GitHub repository (clones or initializes).
*   **`gh-local-purge`**: Purges all checked-in files from a Git repository.
*   **`gh-refresh`**: Regenerates a Git repository.
*   **`gh-dwim-init`**: Initializes a project and pushes it to GitHub.
*   **`gh-dwim-push`**: Pushes changes to GitHub.

### `std.make.makefile` (Makefile Generation)

This sub-namespace provides functions for generating `Makefile` content from Clojure data structures.

**Key Functions:**

*   **`emit-headers`**: Emits `Makefile` headers (variables).
*   **`emit-target`**: Emits a `Makefile` target with its dependencies and commands.
*   **`write`**: Writes a `Makefile` from a Clojure data structure.

### `std.make.project` (Project Management)

This sub-namespace provides functions for managing project configurations, building projects, and tracking changes.

**Core Concepts:**

*   **`def.make` Macro:** Defines a `Makefile` configuration.

**Key Functions:**

*   **`makefile-parse`**: Parses a `Makefile` for its sections.
*   **`build-default`**: Builds the default section of a `Makefile`.
*   **`changed-files`**: Retrieves a list of changed files from a build result.
*   **`is-changed?`**: Checks if a project has changed.
*   **`build-all`**: Builds all sections of a `Makefile`.
*   **`build-at`**: Builds a specific section of a `Makefile`.
*   **`def-make-fn`**: Implementation for `def.make`.
*   **`build-triggered`**: Builds projects based on triggered namespaces.

### `std.make.readme` (README Generation)

This sub-namespace provides functions for generating `README.md` files from Org-mode files, including tangling code blocks.

**Key Functions:**

*   **`has-orgfile?`**: Checks if an Org-mode file exists.
*   **`tangle-params`**: Extracts tangle parameters from an Org-mode file.
*   **`tangle-parse`**: Parses an Org-mode file for tangle blocks.
*   **`tangle`**: Extracts code blocks from an Org-mode file and writes them to separate files.
*   **`make-readme-raw`**: Filters out build-related sections from a README.
*   **`make-readme`**: Generates a `README.md` file from an Org-mode file.

### Usage Pattern:

The `std.make` module is essential for automating various aspects of software development within the `foundation-base` project. It provides:
*   **Unified Build System:** A consistent way to define and execute build steps across different projects and languages.
*   **Project Scaffolding:** Tools for setting up new projects with predefined structures and configurations.
*   **Version Control Integration:** Streamlined workflows for committing, pushing, and managing GitHub repositories.
*   **Documentation Generation:** Automated creation of `README.md` files from Org-mode sources.
*   **Extensibility:** A modular design that allows for custom build steps and integrations.

By offering a comprehensive set of build automation and project management tools, `std.make` simplifies the development lifecycle and promotes consistency across the `foundation-base` ecosystem.