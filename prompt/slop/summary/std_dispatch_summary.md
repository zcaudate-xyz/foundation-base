## std.dispatch: A Comprehensive Summary (including submodules)

The `std.dispatch` module provides a flexible and extensible framework for asynchronous task dispatching and execution in Clojure applications. It offers various dispatch strategies (core, queue, debounce, hub, board) to handle different concurrency patterns, from simple task submission to complex, dependency-aware processing. The module is built on top of `std.concurrent` and `std.lib.component`, ensuring robust lifecycle management and integration with other parts of the `foundation-base` ecosystem.

### `std.dispatch` (Main Namespace)

This namespace serves as the primary entry point for the dispatch system, aggregating and re-exporting key functionalities from its submodules. It defines the core `IDispatch` protocol and provides generic functions for creating and submitting tasks to dispatchers.

**Core Concepts:**

*   **`IDispatch` Protocol:** The central abstraction for dispatchers, defining methods like `-submit` for task submission and `-bulk?` to indicate if the dispatcher handles bulk operations.
*   **`IComponent` Protocol:** Dispatchers are components, allowing them to be started, stopped, and managed within a larger system.
*   **Dispatch Strategies:** The module supports various dispatch types (e.g., `:core`, `:queue`, `:debounce`, `:hub`, `:board`), each optimized for different use cases.

**Key Functions:**

*   **`dispatch?`**: Checks if an object is a dispatcher.
*   **`submit`**: Submits an entry to a dispatcher. It handles serial execution if specified.
*   **`create`**: Creates a component-compatible dispatcher based on a type and options.
*   **`dispatch`**: Creates and starts a dispatcher.

### `std.dispatch.common` (Common Dispatcher Utilities)

This sub-namespace provides shared helper functions and protocols for all dispatcher implementations, ensuring consistency and reducing code duplication.

**Key Functions:**

*   **`to-string`**: Generates a string representation for dispatchers.
*   **`info-base`**: Returns basic information about a dispatcher.
*   **`create-map`**: Creates a base map for dispatcher configuration, including pool options and runtime counters.
*   **`handle-fn`**: A generic function for handling dispatched entries, incorporating hooks for processing, error handling, and completion.
*   **`await-termination`**: Waits for an executor to terminate.
*   **`start-dispatch`, `stop-dispatch`, `kill-dispatch`**: Generic lifecycle functions for dispatchers, managing the underlying `ExecutorService`.
*   **`started?-dispatch`, `stopped?-dispatch`**: Checks the running status of a dispatcher.
*   **`info-dispatch`, `health-dispatch`, `remote?-dispatch`, `props-dispatch`**: Generic introspection functions for dispatchers.
*   **`check-hooks`**: Validates that custom hooks conform to expected argument lists.

### `std.dispatch.hooks` (Dispatcher Hooks and Counters)

This sub-namespace defines a system of hooks and counters that allow for fine-grained monitoring and customization of dispatcher behavior at various stages of task processing.

**Key Functions:**

*   **`counter`**: Creates an atom-based counter map for tracking dispatcher statistics (submit, queued, process, complete, error, etc.).
*   **`inc-counter`, `update-counter`**: Functions for incrementing and updating dispatcher counters.
*   **`handle-entry`**: Invokes a registered hook function.
*   **`on-submit`, `on-queued`, `on-batch`, `on-process`, `on-process-bulk`, `on-skip`, `on-poll`, `on-error`, `on-complete`, `on-complete-bulk`, `on-shutdown`, `on-startup`**: Specific hook functions that are called at different stages of task processing.

### `std.dispatch.core` (Core Dispatcher)

This sub-namespace implements a basic dispatcher that submits tasks directly to an underlying `ExecutorService` for immediate execution. It's suitable for simple, fire-and-forget task submission.

**Key Functions:**

*   **`submit-dispatch`**: Submits an entry to the core dispatcher, executing it on the executor.
*   **`create-dispatch`**: Creates a `CoreDispatch` instance.

### `std.dispatch.queue` (Queue Dispatcher)

This sub-namespace implements a dispatcher that queues tasks and processes them in batches using an underlying `hub` (from `std.concurrent.atom`). It's ideal for scenarios where tasks need to be processed efficiently in groups.

**Key Functions:**

*   **`start-dispatch`**: Starts the queue dispatcher.
*   **`handler-fn`**: Creates a handler function that processes batches of entries from the queue.
*   **`submit-dispatch`**: Submits an entry to the queue dispatcher, adding it to the hub and triggering batch processing.
*   **`create-dispatch`**: Creates a `QueueDispatch` instance.

### `std.dispatch.debounce` (Debounce Dispatcher)

This sub-namespace implements a dispatcher that debounces task submissions, preventing a flood of rapid, repetitive tasks. It supports different debounce strategies: eager, delay, and notify.

**Core Concepts:**

*   **Debounce Strategies:**
    *   `:eager`: Executes the first task immediately, then ignores subsequent tasks for a specified interval.
    *   `:delay`: Waits for a specified interval after the last task submission before executing the task.
    *   `:notify`: Executes the first task immediately, and subsequent tasks within the interval update the task to be executed after the interval.
*   **`wrap-min-time`**: Ensures a handler runs for a minimum duration.

**Key Functions:**

*   **`submit-eager`**: Implements the eager debounce strategy.
*   **`submit-delay`**: Implements the delay debounce strategy.
*   **`submit-notify`**: Implements the notify debounce strategy.
*   **`submit-dispatch`**: Dispatches entries based on the configured debounce strategy.
*   **`start-dispatch`, `stop-dispatch`**: Lifecycle functions for the debounce dispatcher.
*   **`create-dispatch`**: Creates a `DebounceDispatch` instance.

### `std.dispatch.hub` (Hub Dispatcher)

This sub-namespace implements a dispatcher that uses a "hub" (from `std.concurrent.atom`) to group and process tasks, often in conjunction with a debounce mechanism. It's designed for scenarios where tasks need to be collected and processed in batches, potentially with dependencies or shared state.

**Key Functions:**

*   **`process-hub`**: Processes a group of entries from a hub.
*   **`put-hub`**: Adds an entry to a group's hub.
*   **`create-hub-handler`**: Creates a handler for the hub, which processes entries from the hub.
*   **`update-debounce-handler!`**: Updates the handler of the underlying debounce dispatcher.
*   **`create-debounce`**: Creates the debounce dispatcher used by the hub.
*   **`start-dispatch`, `stop-dispatch`, `kill-dispatch`**: Lifecycle functions for the hub dispatcher.
*   **`submit-dispatch`**: Submits an entry to the hub dispatcher, adding it to the appropriate group's hub and triggering the debounce mechanism.
*   **`info-dispatch`, `started?-dispatch`, `stopped?-dispatch`, `health-dispatch`, `props-dispatch`**: Introspection functions for the hub dispatcher.
*   **`create-dispatch`**: Creates a `HubDispatch` instance.

### `std.dispatch.board` (Board Dispatcher)

This sub-namespace implements a dispatcher that manages tasks on a "board," allowing for dependency tracking and ordered execution of tasks based on groups. It's suitable for complex workflows where tasks have interdependencies and need to be processed in a specific order.

**Core Concepts:**

*   **Board:** An atom holding the state of submitted tasks, including their return values, lookup maps, queues, busy status, and dependencies.
*   **Tickets:** Unique identifiers for submitted tasks.
*   **Groups:** Tasks can belong to one or more groups, which are used for dependency tracking.

**Key Functions:**

*   **`get-ticket`**: Generates a unique ticket for a task.
*   **`new-board`**: Creates a new board state.
*   **`submit-ticket`**: Adds a task ticket to the board, associating it with its groups.
*   **`submit-board`**: Submits an entry to the board, assigning a ticket and registering its groups.
*   **`clear-board`**: Clears a task from the board after it's processed.
*   **`add-dependents`**: Adds dependents to a group.
*   **`poll-board`**: Polls the board for tasks that are ready to be executed (i.e., their dependencies are met).
*   **`poll-dispatch`**: Polls the dispatcher for more work, submitting ready tasks to the executor.
*   **`submit-dispatch`**: Submits an entry to the board dispatcher.
*   **`start-dispatch`, `stop-dispatch`**: Lifecycle functions for the board dispatcher.
*   **`create-dispatch`**: Creates a `BoardDispatch` instance.

### `std.dispatch.types` (Contract Definitions for Dispatchers)

This sub-namespace defines Malli schemas (contracts) for various dispatcher configurations, ensuring type safety and valid input for dispatcher creation.

**Key Functions:**

*   **`<queue>`, `<pool>`, `<executor>`**: Schemas for queue, pool, and executor configurations.
*   **`+dispatch+`, `+dispatch:common+`**: Base schemas for dispatcher configurations.
*   **`+options|interval+`, `+options|batch+`, `+options|group+`, `+options|delay+`, `+options|debounce+`**: Schemas for specific dispatcher options.
*   **`<dispatch:core>`, `<dispatch:queue>`, `<dispatch:debounce>`, `<dispatch:hub>`, `<dispatch:board>`**: Specific schemas for each dispatcher type.
*   **`<dispatch>` (multispec)**: A multispec that dispatches to the appropriate dispatcher schema based on the `:type` key.

### Usage Pattern:

The `std.dispatch` module is essential for building responsive and scalable applications that need to handle asynchronous operations efficiently. It provides:
*   **Flexible Task Execution:** Different dispatch strategies to match various concurrency requirements.
*   **Resource Management:** Integration with `std.concurrent.executor` for managing thread pools.
*   **Event-Driven Architectures:** Hooks for reacting to different stages of task processing.
*   **Dependency Management:** The board dispatcher enables complex workflows with inter-task dependencies.
*   **Type Safety:** Contracts ensure that dispatcher configurations are valid.

By offering a rich set of dispatching tools, `std.dispatch` empowers developers to design and implement sophisticated asynchronous processing pipelines.