## std.task: A Comprehensive Summary

The `std.task` module in `foundation-base` provides a framework for defining, executing, and managing tasks, particularly focusing on bulk operations and processing items in a structured manner. It offers utilities for handling task inputs, executing functions with transformations, and presenting results, warnings, errors, and summaries in a user-friendly format.

The module is organized into two main sub-namespaces:

### `std.task.bulk`

This namespace provides the core logic for performing bulk operations, processing items, and presenting the results. It's designed to handle collections of inputs, execute a given function for each, and then aggregate and display the outcomes.

*   **`bulk-display [index-len input-len]`**: Constructs display options for bulk output, defining column widths, colors, and alignment for index, input, data, and time.
*   **`bulk-process-item [f context params lookup env args]`**: Processes a single item within a bulk operation. It executes the function `f` with the item's input, captures exceptions, measures execution time, and optionally prints item-specific output.
*   **`bulk-items-parallel [f inputs context params lookup env args]`**: Processes a collection of inputs in parallel using `pmap`.
*   **`bulk-items-single [f inputs context params lookup env args]`**: Processes a collection of inputs sequentially.
*   **`bulk-items [task f inputs params lookup env args]`**: The main function for orchestrating item processing. It handles input shuffling (if `random` is true), calculates display lengths, and dispatches to either parallel or single-threaded processing based on `parallel` parameter. It also prints a subtitle for the items section.
*   **`bulk-warnings [params items]`**: Filters processed items for warnings (status `:warn`) and optionally prints them in a formatted column.
*   **`bulk-errors [params items]`**: Filters processed items for errors (status `:critical` or `:error`) and optionally prints them in a formatted column.
*   **`prepare-columns [columns outputs]`**: Prepares column definitions for printing, dynamically calculating column lengths based on output data if not explicitly provided.
*   **`bulk-results [task params items]`**: Filters and sorts processed items to present the final results. It applies ignore functions, formats output based on task definitions, and optionally prints results in a formatted table.
*   **`bulk-summary [task params items results warnings errors elapsed]`**: Generates and optionally prints a summary of the bulk operation, including counts of items, results, warnings, errors, and cumulative/elapsed times. It can also aggregate custom metrics defined in the task.
*   **`bulk-package [task bundle return package]`**: Packages the results of the bulk operation into a specified format (e.g., `:all`, `:results`, `:summary`, `:items`) and structure (e.g., `:vector`, `:map`).
*   **`bulk [task f inputs params lookup env & args]`**: The top-level function for executing a bulk task. It orchestrates the entire process: printing titles, processing items, collecting warnings/errors/results, generating a summary, and packaging the final output.

### `std.task.process`

This namespace provides utilities for constructing and invoking tasks, including handling function arity, input selection, and execution wrapping.

*   **`*interrupt*`**: A dynamic var (atom) used as a flag to signal task interruption.
*   **`main-function [func count]`**: Creates a wrapper function that adapts a given `func` to a standardized task execution signature (taking `input`, `params`, `lookup`, `env`, and `args`). It handles different arities of the original function.
*   **`select-filter [selector id]`**: A flexible filtering mechanism that matches an `id` against various `selector` types (functions, vars, strings, symbols, keywords, regex, sets, forms, vectors).
*   **`select-inputs [task lookup env selector]`**: Selects inputs for a task based on a `selector`. If `selector` is `:all`, it uses the task's `:item :list` function to get all possible inputs. Otherwise, it filters the list of inputs using `select-filter`.
*   **`wrap-execute [f task]`**: Wraps a task's execution function `f` with pre- and post-processing steps defined in the task's `:item` configuration. It also handles converting results to `std.lib.result` format for bulk operations.
*   **`wrap-input [f task]`**: Wraps a task's execution function `f` to handle different input types. If the input is a selector (keyword, vector, set, form), it triggers a bulk operation using `bulk/bulk`. If the input is `:list`, it returns the list of all possible inputs. Otherwise, it executes `f` with the single input.
*   **`task-inputs [task & args]`**: Constructs the complete set of inputs (`input`, `params`, `lookup`, `env`) for a task based on its configuration and provided arguments.
*   **`invoke [task & args]`**: The primary function for executing a task. It prepares the task's inputs, wraps the main function with execution and input handling logic, and then executes it. It also manages the `*interrupt*` flag.

**Overall Importance:**

The `std.task` module is crucial for building automated workflows, command-line tools, and data processing pipelines within the `foundation-base` project. It provides:

*   **Structured Task Definition:** Allows tasks to be defined with clear configurations for input listing, execution logic, pre/post-processing, and result presentation.
*   **Efficient Bulk Operations:** Facilitates processing large collections of data or performing repetitive actions, with options for parallel execution and detailed output.
*   **User-Friendly Output:** Generates formatted tables for results, warnings, and errors, along with comprehensive summaries, enhancing the user experience for CLI tools.
*   **Extensibility:** The modular design with configurable functions and wrappers allows for easy customization and extension of task behavior.
*   **Robust Error Handling:** Integrates with `std.lib.result` to capture and report execution outcomes, including exceptions.

By offering a robust and flexible task management framework, `std.task` significantly contributes to the `foundation-base` project's ability to automate complex software engineering tasks and manage its multi-language development ecosystem effectively.
