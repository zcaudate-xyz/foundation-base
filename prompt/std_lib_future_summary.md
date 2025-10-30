## std.lib.future: A Comprehensive Summary

The `std.lib.future` namespace provides a powerful and flexible abstraction for asynchronous and concurrent programming in Clojure, built upon Java's `CompletableFuture`. It offers a rich set of functions and macros for creating, managing, composing, and handling the results of asynchronous computations, including features for timeouts, cancellations, and exception handling.

**Key Features and Concepts:**

1.  **Core Future Creation and Management:**
    *   **`*pools*`**: A delayed atom holding a map of `java.util.concurrent.Executor` instances (e.g., `:default`, `:pooled`, `:async`) for executing futures.
    *   **`completed [v]`**: Creates a `CompletableFuture` that is already completed with a given value `v`.
    *   **`failed [e]`**: Creates a `CompletableFuture` that is already completed exceptionally with a given `Throwable e`.
    *   **`incomplete []`**: Creates a `CompletableFuture` that is not yet completed, similar to a `Promise`.
    *   **`future? [obj]`**: A predicate to check if an object is a `CompletableFuture`.
    *   **`future:fn [f & [m]]`**: Creates a `CompletableFuture` that holds a function `f` and optional metadata `m`, intended for later execution.
    *   **`future:call [obj & [m]]`**: Executes a function `obj` (or a `future:fn` object) asynchronously, returning a `CompletableFuture`. It supports specifying an executor `pool` and a `delay`.
    *   **`future:timeout [future interval & [default]]`**: Adds a timeout to a `CompletableFuture`. If the future doesn't complete within `interval` milliseconds, it either completes exceptionally with a `TimeoutException` or with a `default` value.
    *   **`future:wait [future & [interval]]`**: Blocks the current thread until the `future` completes. Optionally waits for a specified `interval` before returning the future.
    *   **`future:run [obj & [m]]`**: A high-level function to run an asynchronous computation with options for `pool`, `delay`, `timeout`, and `default` value.
    *   **`future:now [future & [default]]`**: Attempts to get the value of the `future` immediately without blocking. If not complete, returns `default` or throws an exception.
    *   **`future:value [future]`**: Blocks and returns the result of the `future`.
    *   **`future:exception [future]`**: Blocks and returns the exception if the `future` completed exceptionally, otherwise `nil`.
    *   **`future:cancel [future]`**: Attempts to cancel the execution of the `future`.
    *   **`future:done [future & body]`**: A macro that ensures a `future` is complete before executing `body`, otherwise throws an exception.
    *   **`future:cancelled? [future]`**: Checks if the `future` was cancelled.
    *   **`future:exception? [future]`**: Checks if the `future` completed exceptionally.
    *   **`future:timeout? [future]`**: Checks if the `future` completed exceptionally due to a `TimeoutException`.
    *   **`future:success? [future]`**: Checks if the `future` completed successfully (not exceptionally).
    *   **`future:incomplete? [future]`**: Checks if the `future` is still running (not yet completed).
    *   **`future:complete? [future]`**: Checks if the `future` has completed (either successfully or exceptionally).
    *   **`future:force [future type object]`**: Forces a `future` to complete with a given `object` (either as a `:value` or an `:exception`).
    *   **`future:obtrude [future type object]`**: Similar to `future:force` but uses `obtrudeValue` and `obtrudeException`, which can overwrite existing results.
    *   **`future:dependents [future]`**: Returns the number of dependent stages waiting on the `future`'s completion.
    *   **`future:nil []`**: A memoized function that returns a completed future with `nil`.
    *   **`future:lift [obj]`**: Lifts a plain value or an existing `CompletableFuture` into a `CompletableFuture`.

2.  **Future Composition and Callbacks (`on:` functions):**
    *   **`on:complete [future f & [m]]`**: Attaches a callback `f` to be executed when the `future` completes, regardless of success or failure. The callback receives both the result and any exception.
    *   **`on:timeout [future f & [m]]`**: Attaches a callback `f` to be executed specifically if the `future` times out.
    *   **`on:cancel [future f & [m]]`**: Attaches a callback `f` to be executed specifically if the `future` is cancelled.
    *   **`on:exception [future f & [m]]`**: Attaches a callback `f` to be executed specifically if the `future` completes exceptionally (for any exception).
    *   **`on:success [future f & [m]]`**: Attaches a callback `f` to be executed only if the `future` completes successfully.
    *   **`on:all [futures & [f m]]`**: Creates a new `CompletableFuture` that completes when all input `futures` have completed. An optional function `f` can process their results.
    *   **`on:any [futures f & [m]]`**: Creates a new `CompletableFuture` that completes when any of the input `futures` completes. An optional function `f` can process the result of the first completed future.

3.  **Macros for Asynchronous Flow Control:**
    *   **`future [& opts? body]`**: A macro that wraps `future:run` to provide a more idiomatic way to define asynchronous blocks of code.
    *   **`then [future bindings & body]`**: A macro that provides a convenient syntax for chaining successful operations (`on:success`) or handling both success and failure (`on:complete`).
    *   **`catch [future bindings & body]`**: A macro that provides a convenient syntax for handling exceptions (`on:exception`).

4.  **Result Handling and Status:**
    *   **`fulfil [future f & [print skip-success]]`**: Attempts to complete an `incomplete` future by executing a function `f`. It handles both successful results and exceptions, optionally printing stack traces.
    *   **`future:result [future]`**: Returns a map describing the final status of a `future` (`:success`, `:error`), its `data`, and any `exception`.
    *   **`future:status [future]`**: Returns a keyword indicating the current status of a `future` (`:waiting`, `:success`, `:error`).
    *   **`future:chain [future chain & [marr]]`**: Chains a sequence of functions (`chain`) to be executed sequentially on the result of a `future`, each step returning a new future.

**Overall Importance:**

The `std.lib.future` module is a cornerstone for building responsive, scalable, and fault-tolerant applications within the `foundation-base` project. Its key contributions include:

*   **Simplified Asynchronous Programming:** Provides a high-level, idiomatic Clojure API over `CompletableFuture`, making it easier to write and reason about asynchronous code.
*   **Robust Concurrency Control:** Offers fine-grained control over execution (executors, delays, timeouts) and robust exception handling mechanisms.
*   **Powerful Composition:** The `on:` functions and `then`/`catch` macros enable complex asynchronous workflows to be built by composing smaller, independent futures.
*   **Improved Responsiveness:** Allows long-running computations to be offloaded to background threads, preventing UI freezes or blocking of critical operations.
*   **Error Resilience:** Built-in support for timeouts, cancellations, and structured exception handling helps in creating more resilient systems.

By offering these comprehensive asynchronous programming capabilities, `std.lib.future` significantly enhances the `foundation-base` project's ability to manage complex, concurrent tasks, which is vital for its multi-language transpilation and runtime management goals.
