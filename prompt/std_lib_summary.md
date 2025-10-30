# `std.lib` Summary

`std.lib` is the foundational utility library for the entire `foundation-base` ecosystem. It functions as a "prelude," aggregating and providing common functions used by all other modules. Its main purpose is to offer a consistent and extended set of core functionalities, tailored for the specific needs of the multi-language transpilation and runtime environment of the project.

**Core Concepts:**

*   **Aggregation:** `std.lib` uses a custom helper, `f/intern-all` (from `std.lib.foundation`), to pull functions from its required sub-modules and make them available directly under the `std.lib` namespace. This provides a single, convenient entry point for developers to access a wide range of utilities without needing to `require` numerous individual namespaces.

*   **Core Shadowing:** `std.lib` explicitly *excludes* and shadows several core Clojure functions, such as `->`, `->>`, `swap!`, `reset!`, `future`, and `memoize`. It provides its own enhanced implementations for these, often with added features like placeholder support in threading macros (`->` and `->>`) or more advanced caching strategies in `memoize`.

*   **Sub-modules:** The actual implementations of the functions are organized into a logical hierarchy of sub-modules. This modular design keeps the codebase clean and maintainable. The main sub-modules include:
    *   `std.lib.atom`: For atomic operations.
    *   `std.lib.collection`: For collection manipulations.
    *   `std.lib.component`: For the component lifecycle model.
    *   `std.lib.deps`: For dependency management.
    *   `std.lib.env`: For environment interactions.
    *   `std.lib.foundation`: For fundamental utilities.
    *   `std.lib.function`: For function-related helpers.
    *   `std.lib.future`: For asynchronous programming.
    *   `std.lib.io`: For input/output operations.
    *   `std.lib.memoize`: For memoization.
    *   `std.lib.security`: For cryptographic functions.
    *   `std.lib.signal`: For a signal/event system.
    *   `std.lib.stream`: For data streaming and transducers.
    *   `std.lib.system`: For managing system components.
    *   `std.lib.time`: For time-related utilities.
    *   `std.lib.trace`: For function tracing and debugging.
    *   `std.lib.transform`: For data transformation.
    *   `std.lib.version`: For version string parsing and comparison.
    *   `std.lib.walk`: For traversing nested data structures.
    *   `std.lib.zip`: For zipper-based navigation and manipulation of data structures.

**Key Areas of Functionality (with Examples):**

*   **Atom Manipulation (`std.lib.atom`):** Provides powerful functions for working with atoms, going beyond Clojure's core offerings.
    *   **`swap-return!`**: Swaps the value of an atom and returns a vector of `[old-value new-value]`.
        ```clojure
        (defonce my-atom (atom 0))
        (swap-return! my-atom (fn [v] [v (inc v)]))
        ;; => [0 1]
        ```
    *   **`atom:get`, `atom:set`**: Functions for getting and setting values within nested maps inside an atom.
        ```clojure
        (defonce nested-atom (atom {:a {:b 1}}))
        (atom:get nested-atom [:a :b])
        ;; => 1
        (atom:set nested-atom [:a :c] 2)
        @nested-atom
        ;; => {:a {:b 1, :c 2}}
        ```

*   **Collections (`std.lib.collection`):** A rich set of functions for working with Clojure's collections.
    *   **`map-keys`, `map-vals`**: For transforming map keys and values.
        ```clojure
        (map-keys #(keyword (clojure.string/upper-case (name %))) {:a 1 :b 2})
        ;; => {:A 1, :B 2}
        (map-vals inc {:a 1 :b 2})
        ;; => {:a 2, :b 3}
        ```
    *   **`merge-nested`**: For deep merging of nested maps.
        ```clojure
        (merge-nested {:a {:b 1}} {:a {:c 2}})
        ;; => {:a {:b 1, :c 2}}
        ```
    *   **`tree-flatten`, `tree-nestify`**: For converting between flat and nested map structures.
        ```clojure
        (tree-flatten {:a {:b {:c 1}}})
        ;; => {:a/b/c 1}
        (tree-nestify {:a/b/c 1})
        ;; => {:a {:b {:c 1}}}
        ```

*   **Concurrency (`std.lib.future`):** A custom `CompletableFuture`-based `future` implementation.
    *   **`future`, `then`, `catch`**: For creating and chaining asynchronous operations.
        ```clojure
        (-> (future (Thread/sleep 100) (+ 1 2))
            (then [result] (* result 2))
            (then [result] (println "Final result:" result)))
        ;; Prints "Final result: 6" after a delay.
        ```

*   **Dependency Management (`std.lib.deps`):** A system for managing dependencies between components.
    *   **`deps-ordered`**: For resolving and ordering dependencies in a dependency graph.
        ```clojure
        (deps-ordered (context {:a #{:b} :b #{:c} :c #{}}))
        ;; => '(:c :b :a)
        ```

*   **Environment (`std.lib.env`):** Functions for interacting with the development and runtime environment.
    *   **`prn`, `pp`, `pl`**: Enhanced printing functions that include namespace and line number information.
        ```clojure
        (pl {:a 1 :b 2})
        ;; Prints the map with line numbers and file info.
        ```
    *   **`meter`**: For measuring the execution time of code blocks.
        ```clojure
        (meter "My expensive operation" (Thread/sleep 100))
        ;; Prints the time taken for the operation.
        ```

*   **System Components (`std.lib.system`):** A hierarchical component model for building applications.
    *   **`system`**: For defining a system of interconnected components.
        ```clojure
        (def topology {:db [MyDatabase] :web [WebServer :db]})
        (def my-system (system topology {:db {:port 5432} :web {:port 8080}}))
        (start my-system)
        ```

*   **Zippers (`std.lib.zip`):** A powerful zipper implementation for navigating and manipulating tree-like data structures.
    *   **`vector-zip`**: For creating a zipper from a vector.
        ```clojure
        (-> (vector-zip [1 [2 3] 4])
            (zip/step-inside)
            (zip/step-right)
            (zip/step-inside)
            (zip/replace-right 99)
            (zip/root-element))
        ;; => [1 [2 99] 4]
        ```
