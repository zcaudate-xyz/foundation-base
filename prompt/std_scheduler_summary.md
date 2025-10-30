## std.scheduler: A Comprehensive Summary (including submodules)

The `std.scheduler` module provides a robust and flexible framework for scheduling and managing asynchronous tasks in Clojure applications. It builds upon `std.concurrent` to leverage thread pools and queues, offering features like program installation, dynamic spawning of tasks, and comprehensive monitoring of task execution. The module is designed to handle various scheduling policies and provides detailed information about running and completed tasks.

### `std.scheduler` (Main Namespace)

This namespace serves as the primary entry point for the scheduling system, aggregating and re-exporting key functionalities from its submodules. It defines the core `Runner` component and provides high-level functions for managing scheduled programs.

**Core Concepts:**

*   **`Runner` Component:** The central component that manages scheduled programs and their execution. It encapsulates the underlying executors and program states.
*   **Programs:** Definitions of tasks to be scheduled, including their type, execution policy, and associated functions.
*   **Spawns:** Individual instances of a running program.

**Key Functions:**

*   **`get-spawn`, `stop-spawn`, `kill-spawn`**: Functions for managing individual spawns.
*   **`get-all-spawn`, `count-spawn`, `list-spawn`, `latest-spawn`, `earliest-spawn`, `list-stopped`, `latest-stopped`, `latest`, `stop-all-spawn`, `kill-all-spawn`, `clear`, `get-state`, `get-program`**: Functions for managing all spawns of a program.
*   **`runner:start`, `runner:stop`, `runner:kill`**: Lifecycle functions for the `Runner`.
*   **`runner:started?`, `runner:stopped?`, `runner:health`, `runner:info`**: Status and introspection functions for the `Runner`.
*   **`runner?`, `runner:create`, `runner`**: Predicate and constructors for `Runner` records.
*   **`installed?`, `create-program`, `uninstall`, `install`**: Functions for managing program definitions.
*   **`spawn`, `unspawn`**: Functions for dynamically starting and stopping program instances.
*   **`trigger`**: Manually triggers a program's execution.
*   **`set-interval`**: Dynamically adjusts the interval of a running program.
*   **`get-props`**: Retrieves properties of running spawns.

### `std.scheduler.common` (Common Scheduler Utilities)

This sub-namespace provides shared helper functions for managing the scheduler's runtime environment, including executor creation and lifecycle management.

**Key Functions:**

*   **`new-runtime`**: Constructs a new runtime environment for the scheduler, including `core` and `scheduler` executors.
*   **`stop-runtime`**: Stops all executors within a runtime.
*   **`kill-runtime`**: Forcefully stops all executors within a runtime.
*   **`all-ids`**: Returns all running program IDs.
*   **`spawn-form`, `gen-spawn` (macro)**: Generates spawn-related forms.
*   **`spawn-all-form`, `gen-spawn-all` (macro)**: Generates forms for all spawns.

### `std.scheduler.spawn` (Task Spawning and Management)

This sub-namespace provides the core logic for spawning and managing individual task instances (spawns), including their lifecycle, job management, and result handling.

**Core Concepts:**

*   **`Spawn` Record:** Represents a single instance of a running program, tracking its ID, properties, output, state, and exit status.
*   **Jobs:** Individual tasks submitted to a spawn.
*   **Program Definitions:** Configurable properties for a scheduled program (e.g., `type`, `policy`, `interval`, `main-fn`, `args-fn`).

**Key Functions:**

*   **`spawn-status`**: Returns the status of a spawn.
*   **`spawn-info`**: Returns detailed information about a spawn.
*   **`spawn?`, `create-spawn`**: Predicate and constructor for `Spawn` records.
*   **`set-props`, `get-props`**: Sets or retrieves properties of a spawn.
*   **`get-job`, `update-job`, `remove-job`, `add-job`, `list-jobs`, `list-job-ids`, `count-jobs`**: Functions for managing jobs within a spawn.
*   **`send-result`**: Sends a result to a spawn's output.
*   **`handler-run`**: Executes a job's main function and handles results.
*   **`create-handler-basic`, `create-handler-constant`, `create-handler`**: Creates different types of job handlers.
*   **`schedule-timing`**: Calculates the timing for the next schedule.
*   **`wrap-schedule`**: Wraps a schedule function.
*   **`spawn-save-past`**: Moves a spawn from running to past.
*   **`spawn-loop`**: Creates the main loop for a spawn.
*   **`run`**: Constructs and starts a spawn loop.
*   **`get-all-spawn`, `get-spawn`, `count-spawn`, `list-spawn`, `latest-spawn`, `earliest-spawn`**: Functions for retrieving running spawns.
*   **`list-stopped`, `latest-stopped`, `latest`**: Functions for retrieving stopped spawns.
*   **`stop-spawn`, `kill-spawn`**: Stops or kills a spawn.
*   **`stop-all`, `kill-all`**: Stops or kills all spawns of a program.
*   **`clear`**: Clears program and past spawn information.
*   **`get-state`, `get-program`**: Retrieves program state or definition.

### `std.scheduler.types` (Scheduler Contracts)

This sub-namespace defines Malli schemas (contracts) for scheduler configurations, ensuring type safety and valid input for program definitions.

**Key Functions:**

*   **`+runtime+`**: Schema for runtime options.
*   **`+spawn+`**: Schema for spawn options.
*   **`+program+`**: Schema for program definitions.
*   **`<program>`**: The main schema for a scheduled program.

### Usage Pattern:

The `std.scheduler` module is essential for building applications that require automated or periodic task execution. It provides:
*   **Flexible Scheduling:** Support for various program types (e.g., `:basic`, `:constant`) and scheduling policies.
*   **Concurrency Management:** Leverages `std.concurrent` for efficient task execution.
*   **Program Lifecycle:** Functions for installing, uninstalling, spawning, and stopping programs.
*   **Monitoring and Introspection:** Detailed information about running and completed tasks.
*   **Extensibility:** A protocol-driven design allows for custom program types and handlers.

By offering a comprehensive and extensible task scheduling framework, `std.scheduler` empowers developers to build robust and reliable automated systems.