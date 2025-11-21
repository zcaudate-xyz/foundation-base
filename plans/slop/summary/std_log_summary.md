## std.log: A Comprehensive Summary (including submodules)

The `std.log` module provides a flexible and extensible logging framework for Clojure applications, designed to handle various logging scenarios from simple console output to complex profiling and tracing. It offers a protocol-based approach, allowing for different logger implementations, and includes features like structured logging, context management, and advanced formatting for console output.

### `std.log` (Main Namespace)

This namespace serves as the primary entry point for the logging system, aggregating and re-exporting key functionalities from its submodules. It provides high-level macros and functions for logging messages at different levels, managing logger instances, and controlling logging behavior.

**Key Re-exported Functions:**

*   From `std.log.common`: `put-logger!`, `set-logger!`, `set-static!`, `set-level!`, `set-context!`.
*   From `std.log.console`: `console-logger`.
*   From `std.log.core`: `identity-logger`, `multi-logger`, `basic-logger`, `step`.
*   From `std.log.form`: `log-meta`, `log-runtime`, `log-context`, `log`, `verbose`, `debug`, `info`, `warn`, `error`, `fatal`.
*   From `std.log.profile`: `spy`, `show`, `trace`, `track`, `status`, `silent`, `action`, `meter`, `block`, `section`, `profile`, `note`, `task`, `todo`.

**Key Functions:**

*   **`create`**: Creates a component-compatible logger.
*   **`logger`**: Creates and starts a logger.
*   **`logger?`**: Checks if an object is a logger.
*   **`with-indent` (macro)**: Executes body with a given indent.
*   **`with-level` (macro)**: Executes body with a given log level.
*   **`with-logger` (macro)**: Executes code with a specified logger.
*   **`with-overwrite` (macro)**: Executes code with a given overwrite context.
*   **`current-context`**: Returns the current logging context.
*   **`with-context` (macro)**: Executes code with a given context.
*   **`with` (macro)**: Enables targeted printing of statements.
*   **`with-logger-basic` (macro)**: Executes code with the basic logger.
*   **`with-logger-verbose` (macro)**: Executes code with a verbose logger.

### `std.log.common` (Common Logging Utilities)

This sub-namespace provides shared helper functions and dynamic variables for managing logging levels, contexts, and logger instances.

**Core Concepts:**

*   **Dynamic Variables:** `*level*`, `*overwrite*`, `*context*`, `*trace*`, `*static*`, `*logger*`, `*logger-basic*`, `*logger-verbose*`.
*   **Log Levels:** `+levels+` maps keywords (e.g., `:debug`, `:info`) to numeric priorities.

**Key Functions:**

*   **`set-static!`, `set-level!`, `set-context!`, `set-logger!`, `put-logger!`**: Functions for setting and updating global logging parameters.
*   **`default-logger`**: Returns the default logger instance.
*   **`basic-logger`**: Returns a basic logger instance.
*   **`verbose-logger`**: Returns a verbose logger instance.

### `std.log.console` (Console Logger)

This sub-namespace implements a console logger that formats and prints log entries to the console, with extensive styling and customization options.

**Core Concepts:**

*   **`+style+`**: A map defining default styling for different parts of a log entry (header, body, meter, status).
*   **`+levels+`**: Defines color schemes for different log levels.
*   **`ConsoleLogger` Record:** The concrete implementation of a console logger.

**Key Functions:**

*   **`style-default`**: Retrieves default styling.
*   **`join-with`**: Joins strings with a separator.
*   **`console-pprint`**: Pretty-prints data for console output.
*   **`console-format-line`**: Formats a single line for console output.
*   **`console-display?`**: Checks if an item should be displayed.
*   **`console-render`**: Renders a log entry based on its style.
*   **`console-header-label`, `console-header-position`, `console-header-date`, `console-header-message`, `console-header`**: Functions for rendering the log header.
*   **`console-meter-trace`, `console-meter-form`, `console-meter`**: Functions for rendering meter-related information.
*   **`console-status-outcome`, `console-status-duration`, `console-status-start`, `console-status-end`, `console-status-props`, `console-status`**: Functions for rendering status information.
*   **`console-body-console-text`, `console-body-console`**: Functions for rendering console text in the body.
*   **`console-body-data-context`, `console-body-data`**: Functions for rendering data in the body.
*   **`console-body-exception`, `console-body`**: Functions for rendering exceptions in the body.
*   **`console-format`**: Formats the entire log entry for console output.
*   **`logger-process-console`**: Processes log entries for console output.
*   **`console-write`**: Writes a log entry to the console.
*   **`console-logger`**: Creates a console logger instance.

### `std.log.core` (Core Logger Implementations)

This sub-namespace provides core logger implementations, including an identity logger (which simply returns the log entry), a multi-logger (which dispatches to multiple loggers), and a basic logger (which prints to `*out*`). It also defines helper functions for processing log entries and exceptions.

**Core Concepts:**

*   **`ILogger` Protocol:** Defines the `-logger-write` method for writing log entries.
*   **`IdentityLogger` Record:** A logger that returns the log entry unchanged.
*   **`MultiLogger` Record:** A logger that dispatches log entries to multiple child loggers.
*   **`BasicLogger` Record:** A logger that prints log entries to `*out*`.

**Key Functions:**

*   **`logger-submit`**: Submits a log entry to a logger's queue.
*   **`logger-process`**: Processes a log entry, applying any `fn/process` transformations.
*   **`logger-enqueue`**: Enqueues a log entry for processing.
*   **`process-exception`**: Converts a `Throwable` into a map for logging.
*   **`logger-message`**: Constructs a standardized log message map.
*   **`logger-start`, `logger-info`, `logger-stop`**: Lifecycle and info functions for loggers.
*   **`logger-init`**: Initializes logger settings.
*   **`identity-logger`**: Creates an identity logger.
*   **`multi-logger`**: Creates a multi-logger.
*   **`log-raw`**: Sends raw data to the logger.
*   **`basic-write`**: Writes a log entry to `*out*`.
*   **`basic-logger`**: Creates a basic logger.
*   **`step` (macro)**: Logs a step in a process.

### `std.log.element` (Log Element Formatting)

This sub-namespace provides helper functions for formatting individual elements of a log entry, such as ANSI styling, headings, and time/duration displays.

**Key Functions:**

*   **`style-ansi`**: Constructs ANSI style codes from a style map.
*   **`style-heading`**: Formats a heading with styling.
*   **`elem-daily-instant`**: Formats a label and instant (date/time).
*   **`elem-ns-duration`**: Formats a label and nanosecond duration.
*   **`elem-position`**: Formats a code position (namespace, function, line, column).

### `std.log.form` (Log Form Macros)

This sub-namespace provides macros for generating log entries from Clojure forms, capturing metadata like line numbers and function names. It also defines macros for different log levels.

**Key Functions:**

*   **`log-meta`**: Captures metadata from a form.
*   **`log-function-name`**: Extracts a function name from a mangled string.
*   **`log-runtime-raw`, `log-runtime`**: Captures runtime information (function, method, filename).
*   **`log-check`**: Checks if a log entry should be processed based on its level.
*   **`to-context`**: Converts various inputs to a log context map.
*   **`log-fn`**: The core function for logging an entry.
*   **`log-form`**: Generates a log entry from a form.
*   **`with-context` (macro)**: Applies a context to the current one.
*   **`log-context`**: Creates a log context form.
*   **`log` (macro)**: The main macro for logging messages.
*   **`log-data-form`, `deflog-data` (macro)**: Macros for logging data at different levels (verbose, debug, info, warn).
*   **`log-error-form`, `deflog-error` (macro)**: Macros for logging errors (error, fatal).

### `std.log.match` (Log Entry Matching)

This sub-namespace provides functions for filtering log entries based on various criteria, such as log level, namespace, function name, and custom filters.

**Key Functions:**

*   **`filter-base`**: Matches a value against a filter (regex, string, function).
*   **`filter-include`**: Checks if a value matches any of the include filters.
*   **`filter-exclude`**: Checks if a value does not match any of the exclude filters.
*   **`filter-value`**: Filters a value based on include, exclude, and ignore criteria.
*   **`match-filter`**: Matches an entry against a filter map.
*   **`match`**: The main function for matching a log entry against logger settings.

### `std.log.profile` (Profiling and Tracing)

This sub-namespace provides macros for profiling code execution, tracing function calls, and displaying various metrics. It integrates with the logging system to output structured performance data.

**Core Concepts:**

*   **Spy:** Logs the start and end of a function's execution, including its duration.
*   **Trace:** Tracks function calls within a nested execution flow.
*   **Meter:** Measures the execution time of a block of code.
*   **Status:** Logs the outcome of an operation.

**Key Functions:**

*   **`spy-fn`**: Constructs a spy function.
*   **`bind-trace`**: Binds a trace ID to the log context.
*   **`spy-form`**: Helper for the `spy` macro.
*   **`defspy` (macro)**: Creates a spy macro.
*   **`on-grey`, `on-white`, `on-color`**: Functions for generating console styles.
*   **`item-style`**: Styles a log entry.
*   **`note`, `spy`, `track`, `status`, `show`, `todo`, `action` (macros)**: Specific profiling and tracing macros.
*   **`meter-fn`**: Constructs a meter function.
*   **`meter-form`**: Helper for the `meter` macro.
*   **`defmeter` (macro)**: Creates a meter macro.
*   **`silent`, `trace`, `profile`, `meter`, `block`, `section`, `task` (macros)**: Specific profiling and tracing macros.

### `std.log.template` (Log Message Templating)

This sub-namespace provides a simple templating system for log messages, allowing messages to be rendered using Mustache-like syntax.

**Key Functions:**

*   **`add-template`, `remove-template`, `has-template?`, `list-templates`**: Manages message templates.
*   **`render-message`**: Renders a log message using a template.

### Usage Pattern:

The `std.log` module is essential for:
*   **Debugging:** Providing detailed information about program execution.
*   **Monitoring:** Tracking application behavior and performance.
*   **Auditing:** Recording important events and actions.
*   **Profiling:** Identifying performance bottlenecks.
*   **Structured Logging:** Outputting log data in a machine-readable format.

By offering a comprehensive and customizable logging solution, `std.log` empowers developers to gain deeper insights into their applications and build more robust and maintainable software.