# `std.task` Reference

The `std.task` library provides a data-driven approach to defining and executing operations, supporting both single-item invocation and bulk processing with customizable inputs, outputs, and reporting.

## Task Map Structure

A task is defined by a map (or a `Task` record) containing the following sections:

### Top Level Keys

*   **:type** `(keyword)`: The task type. Used by `task-defaults` to apply default configurations.
*   **:name** `(string|symbol)`: The name of the task.
*   **:main** `(map)`: Configuration for the main execution function.
*   **:item** `(map)`: Configuration for processing individual items in the pipeline.
*   **:result** `(map)`: Configuration for processing and formatting bulk results.
*   **:summary** `(map)`: Configuration for aggregating and summarizing bulk execution.
*   **:construct** `(map)`: Configuration for setting up the execution environment (inputs, context).
*   **:params** `(map)`: Default parameters to be merged with runtime parameters.
*   **:doc** `(string)`: Documentation string for the task.

### :main (Execution)

Defines the core logic of the task.

*   **:fn** `(function)`: The function to execute. Its signature depends on `:argcount` (default 4 args: `[input params lookup env]`).
*   **:arglists** `(list)`: List of argument signatures for documentation.
*   **:argcount** `(integer)`: The number of arguments the function expects (1 to 4).
    *   4: `[input params lookup env & args]`
    *   3: `[input params env & args]`
    *   2: `[input params & args]`
    *   1: `[input & args]`
*   **:args?** `(boolean)`: If `true`, the function accepts variadic arguments.

### :construct (Setup)

Defines how the task resolves its inputs and environment before execution.

*   **:input** `(fn [task] -> input)`: Resolves the input for the task. Can return a raw input or a keyword like `:list` to trigger the `:item :list` generator.
*   **:env** `(fn [task-with-params] -> map)`: Constructs the environment map passed to the main function.
*   **:lookup** `(fn [task env] -> map)`: Constructs the lookup/context map passed to the main function.

### :item (Pipeline)

Controls the processing lifecycle of individual items, especially during bulk execution.

*   **:list** `(fn [lookup env] -> seq)`: Generates a sequence of inputs when the task input is `:list`.
*   **:pre** `(fn [input] -> input')`: Transformation applied to the input before it reaches the `:main` function.
*   **:post** `(fn [result] -> result')`: Transformation applied to the result returned by the `:main` function.
*   **:output** `(fn [result] -> val)`: Formats the single item result for the final return value (also used for bulk items map).
*   **:display** `(fn [data] -> string)`: Formats the data for display in the CLI bulk table (used if `:print {:item true}`).

### :result (Bulk Results)

Configures how results are processed, filtered, and displayed in bulk mode.

*   **:ignore** `(fn [data] -> boolean)`: Predicate to exclude specific results from the final output list.
*   **:keys** `{[key fn] ...}`: A map of extra keys to compute and add to each result object.
*   **:format** `{[key fn] ...}`: Functions to format specific keys for display (e.g., coloring, truncation).
*   **:sort-by** `(keyword|fn)`: Key or function to sort the results by.
*   **:columns** `(vector of maps)`: Defines the columns for the result table.
    *   **:key** `(keyword)`: The data key to display in this column.
    *   **:length** `(integer)`: The width of the column.
    *   **:color** `(set)`: Style keywords (e.g., `#{:blue :bold}`).
    *   **:align** `(keyword)`: Alignment (e.g., `:right`).
*   **:output** `(fn [data] -> val)`: Transformation for the final result values in the return structure.

### :summary (Bulk Summary)

Configures the aggregation of metrics across all executed tasks.

*   **:aggregate** `{[key [selector-fn accumulator-fn init-value]] ...}`: Custom aggregation logic.
    *   `selector-fn`: Extracts value from a result.
    *   `accumulator-fn`: `(fn [acc val] ...)` updates the running total.
    *   `init-value`: Initial value for the accumulator.
*   **:finalise** `(fn [summary items results] -> summary')`: Final transformation of the summary map before return/display.

---

## Runtime Parameters

When invoking a task (via `invoke` or `bulk`), a `params` map can be provided to control execution behavior.

*   **:bulk** `(boolean)`: If `true`, triggers bulk processing mode.
*   **:parallel** `(boolean)`: If `true`, executes bulk items in parallel (using `pmap`).
*   **:random** `(boolean)`: If `true`, shuffles the input list before processing.
*   **:print** `(map)`: Controls CLI output.
    *   **:function** `(boolean)`: Print function output for single tasks.
    *   **:item** `(boolean)`: Print a row for each processed item.
    *   **:result** `(boolean)`: Print the results table.
    *   **:summary** `(boolean)`: Print the summary block.
*   **:return** `(keyword|vector)`: Specifies what data to return.
    *   `:all`: Returns the full bundle (`:items`, `:results`, `:summary`, `:warnings`, `:errors`).
    *   `:results`: Returns just the processed results (default).
    *   `:items`: Returns the map of items.
    *   Specific keys can be provided in a vector.
*   **:package** `(keyword)`: Controls the return data structure.
    *   `:vector`: Returns results as a list/vector.
    *   `nil` (default): Returns results as a map (usually keyed by input).
*   **:title** `(string|fn)`: A title string (or function producing one) to print before execution.
*   **:order-by** `(keyword|fn)`: Overrides the task's default sort order for results.
