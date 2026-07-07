(ns documentation.std-lib-env
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.env` provides environment and system utilities including:

- Namespace introspection
- Resource management
- Development helpers
- Pretty printing utilities
- Debug facilities
"

[[:chapter {:title "Namespace Utilities" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [ns-sym ns-get require dev?]}]]

[[:chapter {:title "Resource Management" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [sys:resource sys:resource-cached sys:resource-content sys:ns-url sys:ns-file sys:ns-dir]}]]

[[:chapter {:title "Pretty Printing" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [p pr pp-str pp-fn pl prf]}]]

[[:chapter {:title "Debug Utilities" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [dbg-print dbg-global dbg:add-filters dbg:remove-filters wrap-print]}]]

[[:chapter {:title "Local State" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [local:set local:clear local]}]]

[[:chapter {:title "Error Handling" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [throwable-string close]}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_env_summary.md
;; sha256: 9517a14ee4c546e208c2ed85e11bcf21b24cd3f9c8b50532b04475ac7bce79ab
[[:chapter {:title "std.lib.env: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-env-summary-md"}]]

"The `std.lib.env` namespace provides a collection of utility functions and macros for interacting with the Clojure runtime environment, managing resources, and enhancing debugging and logging capabilities. It offers tools for namespace introspection, class path resource loading, dynamic function binding, and advanced printing/metering."

"**Key Features and Concepts:**"

"1.  **Namespace and Symbol Utilities:**\n    *   **`ns-sym []`**: Returns the symbol representing the current namespace.\n    *   **`ns-get [ns k]`**: Resolves and returns the value of a symbol `k` within a given namespace `ns`.\n    *   **`require [sym]`**: A concurrency-safe wrapper around `clojure.core/require`.\n\n2.  **Environment Detection:**\n    *   **`dev? []`**: Checks if the current environment is a development environment (by looking for `nrepl.core`).\n\n3.  **Class Path Resource Management:**\n    *   **`sys:resource [n & [loader]]`**: Finds a resource `n` on the class path, returning its `java.net.URL`.\n    *   **`sys:resource-cached [atom path f]`**: Caches the result of applying a function `f` to a class path resource `path`, invalidating the cache if the resource's modification time changes.\n    *   **`sys:resource-content [path]`**: Reads and caches the content of a class path resource as a string.\n    *   **`sys:ns-url [& [ns]]`**: Gets the `java.net.URL` for a given namespace (or the current one).\n    *   **`sys:ns-file [& [ns]]`**: Gets the file path for a given namespace.\n    *   **`sys:ns-dir [& [ns]]`**: Gets the directory path for a given namespace.\n\n4.  **Resource Management:**\n    *   **`close [obj]`**: Closes any object implementing `java.io.Closeable`.\n\n5.  **Local Function Overrides:**\n    *   **`*local*`**: A dynamic var controlling whether local function overrides are active.\n    *   **`+local+`**: An atom holding a map of locally overridden functions (e.g., `println`, `prn`, `pprint`).\n    *   **`local:set [k v & more]`**: Sets or updates local function overrides in `+local+`.\n    *   **`local:clear [k & more]`**: Clears local function overrides from `+local+`.\n    *   **`local [k & args]`**: Applies the locally overridden function associated with key `k`.\n\n6.  **Enhanced Printing and Debugging:**\n    *   **`p [& args]`**: Shortcut for `(local :println)`.\n    *   **`pp-str [& args]`**: Pretty-prints arguments to a string.\n    *   **`pp-fn [& body]`**: A function that pretty-prints its arguments.\n    *   **`pp [& body]`**: A macro that pretty-prints its arguments, including namespace, line, and column information.\n    *   **`do:pp [v]`**: A `doto`-like macro for `pp`.\n    *   **`pl-add-lines [body & [range pad]]`**: Helper for `pl`, adds line numbers to a string.\n    *   **`pl [body & [range]]`**: A macro that prints `body` with line numbers, including namespace, line, and column information.\n    *   **`do:pl [v]`**: A `doto`-like macro for `pl`.\n    *   **`pl:fn []`**: Creates a function that prints with line numbers.\n    *   **`prn [& body]`**: A macro that prints its arguments (like `clojure.core/prn`), but also includes namespace, line, and column information.\n    *   **`do:prn [& args]`**: A `doto`-like macro for `prn`.\n    *   **`prn:fn []`**: Creates a function that prints with namespace and file info.\n    *   **`prf [v & [no-pad]]`**: Pretty-prints with optional padding.\n    *   **`meter [label & body]`**: A macro that measures and prints the time taken to execute a code block, including namespace, line, and column information.\n    *   **`meter-out [& body]`**: Measures and returns the time taken and the result of a code block.\n    *   **`throwable-string [t]`**: Converts a `Throwable` into a stack trace string.\n    *   **`explode [& body]`**: A macro that executes a body, catches any `Throwable`, and prints its stack trace with context information.\n\n7.  **Conditional Debugging (`dbg`):**\n    *   **`*debug*`**: A dynamic var to globally enable/disable `dbg` output.\n    *   **`+debug+`**: An atom holding a set of filters (regex, string, symbol, function) to selectively enable `dbg` output for specific namespaces or symbols.\n    *   **`match-filter [filt id]`**: A helper function to check if an `id` matches a given filter.\n    *   **`dbg-print [ns-str meta & args]`**: The underlying function for `dbg`, which prints debug information if `*debug*` is true or if the namespace matches a filter.\n    *   **`dbg [& body]`**: A macro for conditional debugging output. It prints arguments along with source location if debugging is enabled globally or for the current namespace.\n    *   **`with:dbg [flag & body]`**: A macro to temporarily bind `*debug*` for a block of code.\n    *   **`dbg-global [& [flag]]`**: Getter/setter for the global `*debug*` flag.\n    *   **`dbg:add-filters [& filters]`**: Adds filters to `+debug+` to enable selective debugging.\n    *   **`dbg:remove-filters [& filters]`**: Removes filters from `+debug+`."

"**Overall Importance:**"

"The `std.lib.env` module is a crucial utility for developers working within the `foundation-base` project. It provides:"

"*   **Enhanced Development Workflow:** Tools like `pp`, `pl`, `prn`, `meter`, and `dbg` significantly improve the debugging and introspection experience, offering more context-rich output than standard Clojure functions.\n*   **Resource Management:** Functions for loading and caching class path resources are essential for managing application assets and configurations.\n*   **Dynamic Environment Control:** The ability to override local functions and control debugging output dynamically allows for flexible adaptation to different development and production needs.\n*   **Robust Error Reporting:** `explode` and `throwable-string` provide better visibility into exceptions, aiding in faster bug resolution."

"By offering these comprehensive environment and debugging utilities, `std.lib.env` contributes significantly to the productivity and maintainability of the `foundation-base` codebase."
;; END merged documentation: plans/slop/summary/std_lib_env_summary.md
