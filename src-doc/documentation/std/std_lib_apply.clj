(ns documentation.std-lib-apply
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.apply` provides applicative invocation, allowing functions to be applied within a configurable runtime context.

Common uses include:

- Invoking a form with a default runtime
- Applying arguments within an explicit context
- Building host applicatives that wrap plain functions"

[[:chapter {:title "Apply within a context" :link "std.lib.apply"}]]

"`apply-in` runs an applicative within a given runtime. `apply-as` picks the runtime automatically."

[[:api {:namespace "std.lib.apply"
        :only [apply-in apply-as invoke-as]}]]

[[:chapter {:title "Host applicatives" :link "std.lib.apply"}]]

"`host-applicative` constructs a plain-function applicative that does not need an external context."

[[:api {:namespace "std.lib.apply"
        :only [host-applicative]}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_apply_summary.md
;; sha256: c8c15101dd320195631eb0ba041dfbfc31c52049716552be84014f86b10f00a1
[[:chapter {:title "std.lib.apply: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-apply-summary-md"}]]

"The `std.lib.apply` namespace provides a mechanism for defining and interacting with \"applicatives\" – a concept often found in functional programming for applying functions within a context. This module is designed to abstract the application of functions, allowing for flexible execution environments and transformations of input and output."

[[:section {:title "Core Concepts:" :link "merged-plans-slop-summary-std-lib-apply-summary-md-core-concepts"}]]

"*   **Applicative:** An applicative is an object that encapsulates a function or a form to be evaluated, along with optional metadata about its execution (e.g., `async`). It implements the `std.protocol.apply/IApplicable` protocol.\n*   **`IApplicable` Protocol:** This protocol defines the interface for applicatives, including:\n    *   `-apply-default`: Determines the default runtime for the applicative if not explicitly provided.\n    *   `-transform-in`: Transforms the input arguments before applying the function.\n    *   `-transform-out`: Transforms the result after the function has been applied.\n*   **Context/Runtime:** Applicatives can operate within a specified runtime (`rt`), which provides the execution context."

[[:section {:title "Key Functions and Macros:" :link "merged-plans-slop-summary-std-lib-apply-summary-md-key-functions-and-macros"}]]

"*   **`apply-in`**:\n    *   **Purpose:** Runs an applicative within a given runtime context. It first transforms the input arguments using `-transform-in`, then applies the function using `-apply-in`, and finally transforms the output using `-transform-out`.\n    *   **Usage:** `(apply-in app rt args)`\n*   **`apply-as`**:\n    *   **Purpose:** Allows an applicative to automatically resolve its runtime context (either from its own `:runtime` field or by calling `-apply-default`).\n    *   **Usage:** `(apply-as app args)`\n*   **`invoke-as`**:\n    *   **Purpose:** A convenience function that invokes an applicative with a variable number of arguments, internally calling `apply-as`.\n    *   **Usage:** `(invoke-as app & args)`\n*   **`host-applicative`**:\n    *   **Purpose:** Constructs a basic `HostApplicative` record that can execute a Clojure function or form directly in the current host environment. It can optionally execute asynchronously using `std.lib.future`.\n    *   **Usage:** `(host-applicative {:form '+ :async true})`\n*   **`HostApplicative` (defimpl record)**:\n    *   **Purpose:** The concrete implementation of `IApplicable` for the host environment. It takes `function`, `form`, and `async` as fields.\n    *   **Protocol Implementation:** Its `-apply-default`, `-transform-in`, and `-transform-out` methods are simple pass-throughs or `nil` for the host context, as no special transformations or default runtimes are needed.\n*   **`host-apply-in`**:\n    *   **Purpose:** A helper function used by `HostApplicative` to perform the actual function application, handling both synchronous and asynchronous execution."

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lib-apply-summary-md-usage-pattern"}]]

"The typical usage involves creating an applicative (e.g., `host-applicative`), and then applying it to arguments using `apply-in`, `apply-as`, or `invoke-as`. This abstraction allows for swapping out the underlying execution mechanism (e.g., local Clojure evaluation, remote execution, or execution within a transpiled language runtime) without changing the core application logic."

[[:section {:title "Example:" :link "merged-plans-slop-summary-std-lib-apply-summary-md-example"}]]

[[:code {:lang "clojure"} ";; Define a host applicative to add numbers\n(def adder (host-applicative {:form '+}))\n\n;; Apply it with arguments\n(apply-in adder nil [1 2 3])\n;; => 6\n\n;; Apply it, letting it resolve its own context (which is none for host-applicative)\n(apply-as adder [10 20])\n;; => 30\n\n;; Invoke it directly\n(invoke-as adder 1 2 3 4 5)\n;; => 15\n\n;; Asynchronous execution\n@(invoke-as (host-applicative {:form '+ :async true}) 1 2 3)\n;; => 6"]]

"This module lays the groundwork for a flexible function application system, crucial for a multi-language and multi-runtime ecosystem like `foundation-base`."
;; END merged documentation: plans/slop/summary/std_lib_apply_summary.md
