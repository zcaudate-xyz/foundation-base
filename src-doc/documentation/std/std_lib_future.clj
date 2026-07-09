(ns documentation.std-lib-future
  (:require [std.lib.future :as f :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.future` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Creating futures"}]]

"`std.lib.future` wraps `CompletableFuture` with a Clojure-friendly API. `future:call` runs a function asynchronously, `completed` creates an already-done future, and `incomplete` creates a promise-like future."

(fact "create and complete futures"
  ^{:refer std.lib.future/completed :added "3.0"}
  @(completed 1)
  => 1

  ^{:refer std.lib.future/future:call :added "3.0"}
  @(f/future:call (fn [] 1))
  => 1

  ^{:refer std.lib.future/incomplete :added "3.0"}
  (f/future:incomplete? (incomplete))
  => true)

[[:section {:title "Timeouts and values"}]]

"`future:timeout` adds a timeout with an optional default. `future:value` blocks for the result, and `future:now` returns a default if not yet complete."

(fact "control future completion"
  ^{:refer std.lib.future/future:timeout :added "3.0"}
  @(-> (f/future:call (fn [] (Thread/sleep 100)))
       (f/future:timeout 10 :ok))
  => :ok

  ^{:refer std.lib.future/future:value :added "3.0"}
  (-> (f/future:run (fn [] 1))
      (f/future:value))
  => 1

  ^{:refer std.lib.future/future:now :added "3.0"}
  (-> (f/future:run (fn [] (Thread/sleep 100)))
      (f/future:now :invalid))
  => :invalid)

[[:section {:title "Exception handling"}]]

"Query and force completion states with `future:exception`, `future:complete?`, and `future:force`."

(fact "handle exceptional completion"
  ^{:refer std.lib.future/future:exception :added "3.0"}
  (-> (f/future:run (fn [] (throw (ex-info "ERROR" {}))))
      (f/future:exception))
  => Throwable

  ^{:refer std.lib.future/future:complete? :added "3.0"}
  (f/future:complete? (completed 1))
  => true)

[[:chapter {:title "API" :link "std.lib.future"}]]

[[:api {:namespace "std.lib.future"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_future_summary.md
;; sha256: dac1d6452422949a909a7c0b873faa8c5cb70af4979f047e4e6bb26d256ae635
[[:chapter {:title "std.lib.future: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-future-summary-md"}]]

"The `std.lib.future` namespace provides a powerful and flexible abstraction for asynchronous and concurrent programming in Clojure, built upon Java's `CompletableFuture`. It offers a rich set of functions and macros for creating, managing, composing, and handling the results of asynchronous computations, including features for timeouts, cancellations, and exception handling."

"**Key Features and Concepts:**"

"1.  **Core Future Creation and Management:**\n    *   **`*pools*`**: A delayed atom holding a map of `java.util.concurrent.Executor` instances (e.g., `:default`, `:pooled`, `:async`) for executing futures.\n    *   **`completed [v]`**: Creates a `CompletableFuture` that is already completed with a given value `v`.\n    *   **`failed [e]`**: Creates a `CompletableFuture` that is already completed exceptionally with a given `Throwable e`.\n    *   **`incomplete []`**: Creates a `CompletableFuture` that is not yet completed, similar to a `Promise`.\n    *   **`future? [obj]`**: A predicate to check if an object is a `CompletableFuture`.\n    *   **`future:fn [f & [m]]`**: Creates a `CompletableFuture` that holds a function `f` and optional metadata `m`, intended for later execution.\n    *   **`future:call [obj & [m]]`**: Executes a function `obj` (or a `future:fn` object) asynchronously, returning a `CompletableFuture`. It supports specifying an executor `pool` and a `delay`.\n    *   **`future:timeout [future interval & [default]]`**: Adds a timeout to a `CompletableFuture`. If the future doesn't complete within `interval` milliseconds, it either completes exceptionally with a `TimeoutException` or with a `default` value.\n    *   **`future:wait [future & [interval]]`**: Blocks the current thread until the `future` completes. Optionally waits for a specified `interval` before returning the future.\n    *   **`future:run [obj & [m]]`**: A high-level function to run an asynchronous computation with options for `pool`, `delay`, `timeout`, and `default` value.\n    *   **`future:now [future & [default]]`**: Attempts to get the value of the `future` immediately without blocking. If not complete, returns `default` or throws an exception.\n    *   **`future:value [future]`**: Blocks and returns the result of the `future`.\n    *   **`future:exception [future]`**: Blocks and returns the exception if the `future` completed exceptionally, otherwise `nil`.\n    *   **`future:cancel [future]`**: Attempts to cancel the execution of the `future`.\n    *   **`future:done [future & body]`**: A macro that ensures a `future` is complete before executing `body`, otherwise throws an exception.\n    *   **`future:cancelled? [future]`**: Checks if the `future` was cancelled.\n    *   **`future:exception? [future]`**: Checks if the `future` completed exceptionally.\n    *   **`future:timeout? [future]`**: Checks if the `future` completed exceptionally due to a `TimeoutException`.\n    *   **`future:success? [future]`**: Checks if the `future` completed successfully (not exceptionally).\n    *   **`future:incomplete? [future]`**: Checks if the `future` is still running (not yet completed).\n    *   **`future:complete? [future]`**: Checks if the `future` has completed (either successfully or exceptionally).\n    *   **`future:force [future type object]`**: Forces a `future` to complete with a given `object` (either as a `:value` or an `:exception`).\n    *   **`future:obtrude [future type object]`**: Similar to `future:force` but uses `obtrudeValue` and `obtrudeException`, which can overwrite existing results.\n    *   **`future:dependents [future]`**: Returns the number of dependent stages waiting on the `future`'s completion.\n    *   **`future:nil []`**: A memoized function that returns a completed future with `nil`.\n    *   **`future:lift [obj]`**: Lifts a plain value or an existing `CompletableFuture` into a `CompletableFuture`.\n\n2.  **Future Composition and Callbacks (`on:` functions):**\n    *   **`on:complete [future f & [m]]`**: Attaches a callback `f` to be executed when the `future` completes, regardless of success or failure. The callback receives both the result and any exception.\n    *   **`on:timeout [future f & [m]]`**: Attaches a callback `f` to be executed specifically if the `future` times out.\n    *   **`on:cancel [future f & [m]]`**: Attaches a callback `f` to be executed specifically if the `future` is cancelled.\n    *   **`on:exception [future f & [m]]`**: Attaches a callback `f` to be executed specifically if the `future` completes exceptionally (for any exception).\n    *   **`on:success [future f & [m]]`**: Attaches a callback `f` to be executed only if the `future` completes successfully.\n    *   **`on:all [futures & [f m]]`**: Creates a new `CompletableFuture` that completes when all input `futures` have completed. An optional function `f` can process their results.\n    *   **`on:any [futures f & [m]]`**: Creates a new `CompletableFuture` that completes when any of the input `futures` completes. An optional function `f` can process the result of the first completed future.\n\n3.  **Macros for Asynchronous Flow Control:**\n    *   **`future [& opts? body]`**: A macro that wraps `future:run` to provide a more idiomatic way to define asynchronous blocks of code.\n    *   **`then [future bindings & body]`**: A macro that provides a convenient syntax for chaining successful operations (`on:success`) or handling both success and failure (`on:complete`).\n    *   **`catch [future bindings & body]`**: A macro that provides a convenient syntax for handling exceptions (`on:exception`).\n\n4.  **Result Handling and Status:**\n    *   **`fulfil [future f & [print skip-success]]`**: Attempts to complete an `incomplete` future by executing a function `f`. It handles both successful results and exceptions, optionally printing stack traces.\n    *   **`future:result [future]`**: Returns a map describing the final status of a `future` (`:success`, `:error`), its `data`, and any `exception`.\n    *   **`future:status [future]`**: Returns a keyword indicating the current status of a `future` (`:waiting`, `:success`, `:error`).\n    *   **`future:chain [future chain & [marr]]`**: Chains a sequence of functions (`chain`) to be executed sequentially on the result of a `future`, each step returning a new future."

"**Overall Importance:**"

"The `std.lib.future` module is a cornerstone for building responsive, scalable, and fault-tolerant applications within the `foundation-base` project. Its key contributions include:"

"*   **Simplified Asynchronous Programming:** Provides a high-level, idiomatic Clojure API over `CompletableFuture`, making it easier to write and reason about asynchronous code.\n*   **Robust Concurrency Control:** Offers fine-grained control over execution (executors, delays, timeouts) and robust exception handling mechanisms.\n*   **Powerful Composition:** The `on:` functions and `then`/`catch` macros enable complex asynchronous workflows to be built by composing smaller, independent futures.\n*   **Improved Responsiveness:** Allows long-running computations to be offloaded to background threads, preventing UI freezes or blocking of critical operations.\n*   **Error Resilience:** Built-in support for timeouts, cancellations, and structured exception handling helps in creating more resilient systems."

"By offering these comprehensive asynchronous programming capabilities, `std.lib.future` significantly enhances the `foundation-base` project's ability to manage complex, concurrent tasks, which is vital for its multi-language transpilation and runtime management goals."
;; END merged documentation: plans/slop/summary/std_lib_future_summary.md
